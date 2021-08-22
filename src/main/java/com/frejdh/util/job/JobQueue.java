package com.frejdh.util.job;

import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.persistence.DaoService;
import com.frejdh.util.job.state.LocalJobWorkerThreadState;
import com.frejdh.util.job.util.JobQueueLogger;
import lombok.extern.log4j.Log4j2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Log4j2
public class JobQueue {

	private static final Logger LOGGER = JobQueueLogger.getLogger();
	protected ThreadPoolExecutor pool;
	protected static ThreadLocal<LocalJobWorkerThreadState> threadState = new ThreadLocal<>();
	protected volatile QueueOptions options;
	protected volatile DaoService daoService = new DaoService();
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	JobQueue() {
		this(QueueOptions.getDefault());
	}

	JobQueue(QueueOptions options) {
		this(options, null);
	}

	JobQueue(QueueOptions options, List<Job> jobs) {
		this.pool = (ThreadPoolExecutor) createThreadPool();
		this.options = options;
		if (jobs != null) {
			jobs.forEach(this::add);
		}
	}

	private ExecutorService createThreadPool() {
		if (options.isCachedThreadPool()) {
			return new ThreadPoolExecutor(
					0,
					options.getMaxAmountOfThreads(),
					30L, TimeUnit.SECONDS,
					new SynchronousQueue<>()
			);
		}
		return Executors.newFixedThreadPool(options.getMaxAmountOfThreads());
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

	public void add(Job job) {
		if (job != null) {
			setOnErrorForJob(job);
			setJobStatus(job, JobStatus.INITIALIZED);
			job.setOnJobStatusChange(() -> {
				if (options.isDebugMode()) {
					LOGGER.info(String.format("Job with ID: [%s] was updated to the new status [%s]", job.getJobId(), job.getStatus()));
				}
				daoService.updateJob(job);
			});
			daoService.addToPendingJobs(job);
			runScheduler(false);
		}
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
	 *
	 * @param job Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Job job) {
		if (job == null) {
			return false;
		}

		final Future<?> future = daoService.getCurrentJobFuturesByJobId(job.getJobId());
		if (future == null) {
			return false;
		}

		future.cancel(true);
		cancelCurrentJob(job);
		return true;
	}

	/**
	 * Stop a running job (forcefully)
	 *
	 * @param jobId ID of the Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Long jobId) {
		final Job job = daoService.getJobById(jobId);
		return stop(job);
	}

	private boolean addToCurrentJobs(Job job) {
		Job retval = daoService.updateJobOnlyOnFreeResource(job);
		boolean wasAdded = retval != null;
		if (wasAdded) {
			setJobStatus(retval, JobStatus.ADDED_TO_QUEUE);
			return true;
		}
		return false;
	}

	void runScheduler(boolean isRunningOnWorkerThread) {
		List<Job> jobsToCheck = new ArrayList<>(daoService.getPendingJobs().values());
		for (Job job : jobsToCheck) {
			if (daoService.addToCurrentJobs(job)) {
				if (!isRunningOnWorkerThread) {
					Future<?> future = pool.submit(() -> executeJob(job));
					daoService.setCurrentJobFuturesByJobId(job.getJobId(), future);
				}
				else {
					executeJob(job);
				}
			}
			else {
				setJobStatus(job, JobStatus.WAITING_FOR_RESOURCE);
			}
		}
	}

	/**
	 * Executes the job. Only ever executed by worker threads.
	 */
	private void executeJob(Job job) {
		long jobTimeout = job.getJobOptions().getTimeout();
		if (jobTimeout != 0) {
			try {
				daoService.getCurrentJobFuturesByJobId(job.getJobId()).get(jobTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException ignored) {
			}
		}
		job.start();
		runScheduler(true);
	}

	private void setJobStatus(Job job, JobStatus status) {
		job.setStatus(status);
	}

	private void setCurrentJobFutureForThreadState(Future<?> future) {
		threadState.set(LocalJobWorkerThreadState.builder().withJobExecutionFuture(future).build());
//		daoService.updateJob(job);
		runScheduler(true);
	}

	/**
	 * Helper method. Removes the current job.
	 *
	 * @param job Job to remove
	 */
	private synchronized void cancelCurrentJob(Job job) {
		job.setStatus(JobStatus.CANCELED);
	}

	public Job getJobById(Long id) {
		return daoService.getJobById(id);
	}

	public Job getPendingJobById(Long jobId) {
		return daoService.getPendingJobById(jobId);
	}

	public List<Job> getPendingJobsByResource(String resource) {
		return daoService.getPendingJobsByResource(resource);
	}

	public Job getRunningJobById(Long jobId) {
		return daoService.getCurrentJobById(jobId);
	}

	public Map<String, Job> getCurrentJobsForResources() {
		return daoService.getCurrentJobsForResources();
	}

	public Job getCurrentJobByResource(String resource) {
		return daoService.getCurrentJobByResource(resource);
	}

	public Map<Long, Job> getFinishedJobsById() {
		return daoService.getFinishedJobsById();
	}

	public List<Job> getFinishedJobsByResource(String resource) {
		return daoService.getFinishedJobsByResource(resource);
	}

}
