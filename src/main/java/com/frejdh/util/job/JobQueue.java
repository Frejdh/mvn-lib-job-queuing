package com.frejdh.util.job;

import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.DaoService;
import com.frejdh.util.job.state.LocalJobWorkerThreadState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class JobQueue {

	protected ThreadPoolExecutor pool;
	protected QueueOptions options;
	protected DaoService daoService = new DaoService();;
	protected final ThreadLocal<LocalJobWorkerThreadState> threadState = new ThreadLocal<>();
	private long lastJobId;

	JobQueue() { }

	JobQueue(@Nullable List<Job> persistedJobs) {
		this();
		if (persistedJobs != null) {
			addJobDependingOnStatus(persistedJobs);
		}
		this.lastJobId = finishedJobs.values().stream().mapToLong(Job::getJobId).max().orElse(0);
	}

	JobQueue(QueueOptions options, @Nullable List<Job> persistedJobs) {
		this(persistedJobs);
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(options.getAmountOfThreads());
		this.options = options;
	}

	public static JobQueueBuilder getBuilder() {
		return new JobQueueBuilder();
	}

	public void start() {
		runScheduler(false);
		if (options.isSingleExecution()) {
			pool.shutdown();
		}
	}

	/**
	 * Stops the queue. Waits for job executions to be finished
	 */
	public void stop() {
		pool.shutdown();
	}

	/**
	 * Stops the queue now. Doesn't wait for job executions to be finished
	 */
	public void stopNow() {
		pool.shutdownNow();
	}

	public boolean stopAndAwait(long timeout, TimeUnit timeUnit) {
		pool.shutdown();
		boolean completedExecutions = false;
		try {
			completedExecutions = pool.awaitTermination(timeout, timeUnit);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return completedExecutions;
	}

	private void addJobDependingOnStatus(@NotNull List<Job> jobs) {
		jobs.forEach(job -> {
			if (job.isFinished()) {
				finishedJobs.put(job.getJobId(), job);
			}
			else {
				pendingJobs.put(job.getJobId(), job);
			}
		});
	}

	public void add(Job job) {
		synchronized (pendingJobs) {
			job.setJobId(lastJobId++);
			this.pendingJobs.put(job.getJobId(), job);
			job.setStatus(JobStatus.ADDED_TO_QUEUE);
			runScheduler(false);
		}
	}

	/**
	 * Stop a running job (forcefully)
	 * @param job Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Job job, boolean freeResourceWhenStopped) {
		if (job == null) {
			return false;
		}

		final Future<?> future = currentJobFuturesByJobId.get(job.getJobId());
		if (future == null) {
			return false;
		}

		future.cancel(true);
		job.setStatus(JobStatus.CANCELED);
		if (freeResourceWhenStopped) {
			removeCurrentJob(job);
		}

		return true;
	}

	/**
	 * Stop a running job (forcefully)
	 * @param jobId ID of the Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Long jobId, boolean freeResourceWhenStopped) {
		final Job job = currentJobsById.get(jobId);
		return stop(job, freeResourceWhenStopped);
	}

	/**
	 * Attempt to add a job to the "current" job list/mapping, also removes from the "pending" jobs list/mapping.
	 * @param job Job to add
	 * @return True if the job was added, false if the resource was busy.
	 */
	private boolean addToCurrentJobs(Job job) {
		synchronized (currentJobsByResource) {
			if (currentJobsByResource.putIfAbsent(job.getResourceKey(), job) != null) {
				return false;
			}
			this.currentJobsById.put(job.getJobId(), job);
			this.pendingJobs.remove(job.getJobId());
		}
		return true;
	}

	void runScheduler(boolean isRunningOnWorkerThread) {
		for (Job job : pendingJobs.values()) {
			if (addToCurrentJobs(job)) {
				if (!isRunningOnWorkerThread) {
					Future<?> future = pool.submit(() -> executeJob(job));
					threadState.get().jobExecutionFuture = future;
					currentJobFuturesByJobId.put(job.getJobId(), future);
				}
				else {
					currentJobFuturesByJobId.put(job.getJobId(), threadState.get().jobExecutionFuture);
					executeJob(job);
				}
			}
			else {
				job.setStatus(JobStatus.WAITING_FOR_RESOURCE);
			}
		}
	}

	private void executeJob(Job job) {
		// TODO: Fix concurrency!!!
		long jobTimeout = job.getJobOptions().getTimeout();
		if (jobTimeout != 0) {
			try {
				currentJobFuturesByJobId.get(job.getJobId()).get(jobTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {

			}
		}
		job.start();
		removeCurrentJob(job);
		runScheduler(true);
	}

	/**
	 * Helper method. Removes the current job.
	 * @param job Job to remove
	 */
	private synchronized void removeCurrentJob(Job job) {
		this.currentJobsById.remove(job.getJobId());
		this.currentJobsByResource.remove(job.getResourceKey());
		this.currentJobFuturesByJobId.remove(job.getJobId());
		this.finishedJobs.put(job.getJobId(), job);
	}

	public Job getJobById(Long id) {
		return pendingJobs.getOrDefault(id,
				currentJobsById.getOrDefault(id,
						finishedJobs.get(id)
				)
		);
	}

	public Map<Long, Job> getPendingJobsById() {
		return new LinkedHashMap<>(pendingJobs);
	}

	public Job getPendingJobById(Long jobId) {
		return pendingJobs.get(jobId);
	}

	public List<Job> getPendingJobsByResource(String resource) {
		return pendingJobs.values()
				.stream()
				.filter(job -> job.getResourceKey().equals(resource))
				.collect(Collectors.toList());
	}

	public Job getRunningJobById(Long jobId) {
		return currentJobsById.get(jobId);
	}

	public Map<Long, Job> getRunningJobsById() {
		return new LinkedHashMap<>(currentJobsById);
	}

	public Map<String, Job> getRunningJobsByResource() {
		return new LinkedHashMap<>(currentJobsByResource);
	}

	public Job getRunningJobByResource(String resource) {
		return currentJobsByResource.get(resource);
	}

	public Map<Long, Job> getFinishedJobsById() {
		return new LinkedHashMap<>(finishedJobs);
	}

	public List<Job> getFinishedJobsByResource(String resource) {
		return finishedJobs.values()
				.stream()
				.filter(job -> job.getResourceKey().equals(resource))
				.collect(Collectors.toList());
	}

}
