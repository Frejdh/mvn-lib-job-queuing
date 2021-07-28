package com.frejdh.util.job;

import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.DaoService;
import com.frejdh.util.job.state.LocalJobWorkerThreadState;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static com.frejdh.util.common.toolbox.CommonUtils.sneakyThrow;

public class JobQueue {

	protected ThreadPoolExecutor pool;
	protected QueueOptions options;
	protected DaoService daoService = new DaoService();;
	protected static ThreadLocal<LocalJobWorkerThreadState> threadState = new ThreadLocal<>();

	JobQueue() { }

	JobQueue(@Nullable List<Job> persistedJobs) {
		setCurrentJobFutureForThreadState(null);
		if (persistedJobs != null) {
			daoService.addJobDependingOnStatus(persistedJobs);
		}
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

	public void add(Job job) {
		daoService.addToPendingJobs(job);
		runScheduler(false);
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

		final Future<?> future = daoService.getCurrentJobFuturesByJobId(job.getJobId());
		if (future == null) {
			return false;
		}

		future.cancel(true);
		job.setStatus(JobStatus.CANCELED);
		if (freeResourceWhenStopped) {
			daoService.addToFinishedJobs(job);
		}

		return true;
	}

	/**
	 * Stop a running job (forcefully)
	 * @param jobId ID of the Job to stop
	 * @param freeResourceWhenStopped Free the resource or not. Default behavior is 'true'.
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Long jobId, boolean freeResourceWhenStopped) {
		final Job job = daoService.getJobById(jobId);
		return stop(job, freeResourceWhenStopped);
	}

	/**
	 * Stop a running job (forcefully)
	 * @param jobId ID of the Job to stop
	 * @return True if the job was stopped, false if the job was not found or running.
	 */
	public boolean stop(Long jobId) {
		return stop(jobId, true);
	}

	void runScheduler(boolean isRunningOnWorkerThread) {
		for (Job job : daoService.getPendingJobs().values()) {
			if (daoService.addToCurrentJobs(job)) {
				System.out.println("0: " + job.getJobId());
				if (!isRunningOnWorkerThread) {
					System.out.println("0.1: " + job.getJobId());
					Future<?> future = pool.submit(() -> executeJob(job));
					daoService.setCurrentJobFuturesByJobId(job.getJobId(), future);
				}
				else {
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
		System.out.println("1: " + job.getJobId());
		if (jobTimeout != 0) {
			try {
				daoService.getCurrentJobFuturesByJobId(job.getJobId()).get(jobTimeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException ignored) { }
		}
		System.out.println("2: " + job.getJobId());
		job.start();
		System.out.println("3: " + job.getJobId());
		daoService.addToFinishedJobs(job);
		runScheduler(true);
	}

	private void setCurrentJobFutureForThreadState(Future<?> future) {
		threadState.set(LocalJobWorkerThreadState.builder().withJobExecutionFuture(future).build());
	}

}
