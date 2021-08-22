package com.frejdh.util.job;

import com.frejdh.util.job.model.QueueOptions;
import com.frejdh.util.job.model.callables.JobOnError;
import lombok.SneakyThrows;

import java.util.List;

public class JobQueueBuilder {

	private final QueueOptions.QueueOptionsBuilder queueOptionsBuilder;
	private List<Job> predefinedJobs;

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

	public JobQueueBuilder withCachedThreadPool(boolean useCache) {
		queueOptionsBuilder.withCachedThreadPool(useCache);
		return this;
	}

	public JobQueue build() {
		QueueOptions queueOptions = queueOptionsBuilder.build();
		return new JobQueue(queueOptions, predefinedJobs);
	}

	public JobQueue buildAndStart() {
		JobQueue retval = build();
		retval.start();
		return retval;
	}

	public JobQueueBuilder runOnceOnly() {
		queueOptionsBuilder.withSingleExecution(true);
		return this;
	}
}
