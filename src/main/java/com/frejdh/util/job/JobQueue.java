package com.frejdh.util.job;

import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.persistence.DaoService;
import com.frejdh.util.job.state.LocalJobWorkerThreadState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class JobQueue {

	protected ThreadPoolExecutor pool;
	protected volatile QueueOptions options;
	protected volatile DaoService daoService = new DaoService();
	protected final ThreadLocal<LocalJobWorkerThreadState> threadState = new ThreadLocal<>();
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	JobQueue() {
		this(QueueOptions.getDefault());
	}

	JobQueue(QueueOptions options) {
		this(options, null);
	}

	JobQueue(QueueOptions options, List<Job> jobs) {
		this.threadState.set(new LocalJobWorkerThreadState());
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(options.getAmountOfThreads());
		this.options = options;
		if (jobs != null) {
			jobs.forEach(this::add);
		}
	}

	public static JobQueueBuilder getBuilder() {
		return new JobQueueBuilder();
	}

	public void start() {
		runScheduler(false);
		if (options.isSingleExecution()) {
			stop();
		}
	}

	/**
	 * Stops the queue. <i>Waits</i> for job executions to be finished
	 */
	public void stop() {
		pool.shutdown();
	}

	/**
	 * Stops the queue now. <i>Doesn't wait</i> for job executions to be finished
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

	public Job add(Job job) {
		if (job != null) {
			setOnErrorForJob(job);
			job.setOnJobStatusChange(() -> daoService.updateJob(job));
			job.setStatus(JobStatus.INITIALIZED);
			Job generatedJob = daoService.addJob(job);
			runScheduler(false);
			return generatedJob;
		}
		return null;
	}

	private void setOnErrorForJob(Job job) {
		if (options.getOnJobError() != null) {
			final JobOnError currentOnError = job.getOnJobError();
			final JobOnError newOnError = (error) -> {
				options.getOnJobError().onError(error);
				if (currentOnError != null) {
					currentOnError.onError(error);
				}
			};

			job.setOnJobError(newOnError);
		}
	}

	/**
	 * Stop a running job (forcefully)
	 * @param job Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Job job) {
		if (job == null) {
			return false;
		}

		final Future<?> future = currentJobFuturesByJobId.get(job.getJobId());
		if (future == null) {
			return false;
		}

		future.cancel(true);
		cancelCurrentJob(job);

		return true;
	}

	/**
	 * Stop a running job (forcefully)
	 * @param jobId ID of the Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Long jobId) {
		final Job job = daoService.getJobById(jobId);
		return stop(job);
	}

	/**
	 * Attempt to add a job to the "current" job list/mapping, also removes from the "pending" jobs list/mapping.
	 * @param job Job to add
	 * @return True if the job was added, false if the resource was busy.
	 */
	private boolean addToCurrentJobs(Job job) {
		Job retval = daoService.updateJobOnlyOnFreeResource(job);
		boolean wasAdded = retval != null;
		if (wasAdded) {
			retval.setStatus(JobStatus.ADDED_TO_QUEUE);
			return true;
		}
		return false;
	}

	void runScheduler(boolean isRunningOnWorkerThread) {
		for (Job job : daoService.getPendingJobs().values()) {
			if (addToCurrentJobs(job)) {
				if (!isRunningOnWorkerThread) {
					Object lock = new Object();
					Future<?> future = pool.submit(() -> executeJob(job, lock));
					threadState
							.get()
							.jobExecutionFuture = future;
					currentJobFuturesByJobId.put(job.getJobId(), future);
					synchronized (lock) {
						lock.notify();
					}
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
		executeJob(job, null);
	}

	/**
	 * Executes the job. Only ever executed by worker threads.
	 */
	private void executeJob(Job job, Object lock) {
		long jobTimeout = job.getJobOptions().getTimeout();
		if (jobTimeout > 0 && lock != null) {
			try {
				lock.wait(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		job.start();
		daoService.updateJob(job);
		runScheduler(true);
	}

	/**
	 * Helper method. Removes the current job.
	 * @param job Job to remove
	 */
	private synchronized void cancelCurrentJob(Job job) {
		job.setStatus(JobStatus.CANCELED);
	}

	public Job getJobById(Long id) {
		return daoService.getJobById(id);
	}

	public Map<Long, Job> getPendingJobs() {
		return daoService.getPendingJobs();
	}

	public Job getPendingJobById(Long jobId) {
		return daoService.getPendingJobById(jobId);
	}

	public List<Job> getPendingJobsByResource(String resource) {
		return daoService.getPendingJobsByResource(resource);
	}

	public Job getRunningJobById(Long jobId) {
		return daoService.getRunningJobById(jobId);
	}

	public Map<Long, Job> getRunningJobs() {
		return daoService.getRunningJobs();
	}

	public Map<String, Job> getRunningJobsForResources() {
		return daoService.getRunningJobsForResources();
	}

	public Job getRunningJobByResource(String resource) {
		return daoService.getRunningJobByResource(resource);
	}

	public Map<Long, Job> getFinishedJobsById() {
		return daoService.getFinishedJobsById();
	}

	public List<Job> getFinishedJobsByResource(String resource) {
		return daoService.getFinishedJobsByResource(resource);
	}

}
