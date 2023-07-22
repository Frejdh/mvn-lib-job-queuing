package com.frejdh.util.job;

import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.persistence.JobQueueService;
import com.frejdh.util.job.state.LocalJobWorkerThreadState;
import com.frejdh.util.job.util.JobQueueLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class JobQueue {

	private static final Logger LOGGER = JobQueueLogger.getLogger();
	protected ThreadPoolExecutor pool;
	protected static ThreadLocal<LocalJobWorkerThreadState> threadState = new ThreadLocal<>();
	protected volatile QueueOptions options;
	protected final JobQueueService daoService;
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	JobQueue(JobQueueService daoService, QueueOptions options, List<Job> jobs) {
		this.daoService = daoService;
		this.pool = (ThreadPoolExecutor) createThreadPool(options);
		this.options = options;
		if (jobs != null) {
			jobs.forEach(this::add);
		}
	}

	private ExecutorService createThreadPool(QueueOptions options) {
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
			setGlobalOnErrorForJob(job);
			job.appendOnJobStatusChange((jobReference) -> {
				if (options.isDebugMode()) {
					String jobId = (Job.UNASSIGNED_VALUE == job.getJobId() ? "UNASSIGNED" : Long.toString(job.getJobId()));
					LOGGER.info(String.format("Job with ID: [%s] was updated to the new status [%s]", jobId, job.getStatus()));
				}
				daoService.upsertJob(job);
			});

			if (job.getStatus().isWaitingForId()) {
				job.setOnJobIdSetCallback(this::initializeJobAndAddToPending);
			}
			else {
				initializeJobAndAddToPending(job);
			}

		}
	}

	/**
	 * Helper method.
	 */
	private void initializeJobAndAddToPending(Job job) {
		setJobStatus(job, JobStatus.INITIALIZED);
		daoService.upsertJob(job);
		runScheduler(false);
	}

	private void setGlobalOnErrorForJob(Job job) {
		if (options.getOnJobError() != null) {
			job.prependOnJobError(options.getOnJobError());
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

		final Future<?> future = daoService.getRunningJobFutureByJobId(job.getJobId());
		if (future == null) {
			return false;
		}

		future.cancel(true);
		cancelRunningJob(job);
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

	void runScheduler(boolean isRunningOnWorkerThread) {
		List<Job> jobsToCheck = new ArrayList<>(daoService.getPendingJobs().values());
		for (Job job : jobsToCheck) {
			if (!jobIsReadyToBeStarted(job)) {
				continue;
			}

			synchronized (daoService) {
				if (daoService.isResourceFreeForJob(job.getResourceKey())) {
					try {
						if (!isRunningOnWorkerThread) {
							Future<?> future = pool.submit(() -> executeJob(job));
							daoService.setRunningJobFutureByJobId(job.getJobId(), future);
						} else {
							executeJob(job);
						}
					} catch (RejectedExecutionException e) {
						LOGGER.warning("Job queue thread pool stopped, or full [is running: " + !pool.isShutdown() +
								", current size: " + pool.getPoolSize() + ", max size: " + pool.getMaximumPoolSize() + "]");
						break;
					}
				}
				else {
					setJobStatus(job, JobStatus.WAITING_FOR_RESOURCE);
				}
			}
		}
	}

	private boolean jobIsReadyToBeStarted(Job job) {
		return job.getStatus().isPendingAndReady();
	}

	/**
	 * Executes the job. Only ever executed by worker threads.
	 */
	private void executeJob(Job job) {
		long jobTimeout = job.getJobOptions().getTimeout();
		if (jobTimeout != 0) {
			try {
				daoService.getRunningJobFutureByJobId(job.getJobId()).get(jobTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException ignored) {
			}
		}
		job.start();
		daoService.removeRunningJobFutureByJobId(job.getJobId());
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
	private synchronized void cancelRunningJob(Job job) {
		job.setStatus(JobStatus.CANCELED);
	}

	public Job getJobById(Long id) {
		return daoService.getJobById(id);
	}

	public List<Job> getAllJobs() {
		return daoService.getAllJobs();
	}

}
