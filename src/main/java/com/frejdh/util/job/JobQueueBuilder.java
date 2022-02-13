package com.frejdh.util.job;

import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import com.frejdh.util.job.persistence.JobQueueService;
import lombok.SneakyThrows;

import java.util.List;

public class JobQueueBuilder {

	private final QueueOptions.QueueOptionsBuilder queueOptionsBuilder;
	private List<Job> predefinedJobs;
	private JobQueueService jobQueueService = null;

	public JobQueueBuilder() {
		queueOptionsBuilder = QueueOptions.builder();
	}

	public static JobQueueBuilder getInstance() {
		return new JobQueueBuilder();
	}

	/**
	 * Set the number of threads to utilize.
	 * <strong>Default value is {@link QueueOptions#DEFAULT_MAX_AMOUNT_OF_THREADS}</strong>.
	 * @return This builder reference.
	 */
	public JobQueueBuilder withMaxAmountOfThreads(int amountOfThreads) {
		queueOptionsBuilder.withMaxAmountOfThreads(amountOfThreads);
		return this;
	}

	/**
	 * Set a shared on error handler. Executes <i>before</i> the individual job's on error handler (if any).
	 * @return This builder reference.
	 */
	@SneakyThrows
	public JobQueueBuilder withOnErrorHandler(JobOnError onErrorHandler) {
		queueOptionsBuilder.withOnJobError(onErrorHandler);
		return this;
	}

	public JobQueueBuilder withPredefinedJobs(List<Job> predefinedJobs) {
		this.predefinedJobs = predefinedJobs;
		return this;
	}

	public JobQueueBuilder withDebugMode(boolean enable) {
		queueOptionsBuilder.withDebugMode(enable);
		return this;
	}

	/**
	 * Determines if the Job Queue should use a cached thread pool by default.
	 * In other words, threads are started when needed, or reused depending on the situation.
	 * Recommended for queue's that doesn't require all threads to be active at all times.
	 * @param useCache Default value is: {@link QueueOptions#DEFAULT_USE_CACHED_THREAD_POOL}.
	 * @return This builder reference.
	 */
	public JobQueueBuilder withCachedThreadPool(boolean useCache) {
		queueOptionsBuilder.withCachedThreadPool(useCache);
		return this;
	}

	public JobQueueBuilder withCustomDaoService(AbstractJobQueueDao jobQueueDao) {
		jobQueueService = (jobQueueDao != null) ? this.jobQueueService = new JobQueueService(jobQueueDao) : null;
		return this;
	}

	public JobQueueBuilder withCustomDaoService(JobQueueService jobQueueService) {
		this.jobQueueService = jobQueueService;
		return this;
	}

	public JobQueue build() {
		QueueOptions queueOptions = queueOptionsBuilder.build();
		JobQueueService daoService = (jobQueueService != null) ? jobQueueService : new JobQueueService();
		return new JobQueue(daoService, queueOptions, predefinedJobs);
	}

	public JobQueue buildAndStart() {
		JobQueue retval = build();
		retval.start();
		return retval;
	}

	/**
	 * Run the job queue only once. Please note that jobs set with "after build job IDs" will never be executed.
	 * The {@link com.frejdh.util.job.model.JobStatus#WAITING_FOR_ID} is not a ready state for job executions.
	 * @return This builder reference.
	 */
	public JobQueueBuilder runOnceOnly() {
		queueOptionsBuilder.withSingleExecution(true);
		return this;
	}
}
