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
	 * Set the max number of parallel jobs per thread.
	 * A value of 4 jobs on 2 threads, means that it can be up to 8 <i>(4*2)</i> concurrent jobs at any time.
	 * <strong>Default value is {@link QueueOptions#DEFAULT_MAX_PARALLEL_JOBS}</strong>.
	 * @return This builder reference.
	 */
	public JobQueueBuilder withMaxParallelJobs(int maxParallelJobs) {
		queueOptionsBuilder.withMaxParallelJobs(maxParallelJobs);
		return this;
	}

	/**
	 * Set the number of threads to utilize.
	 * <strong>Default value is {@link QueueOptions#DEFAULT_AMOUNT_OF_THREADS}</strong>.
	 * @return This builder reference.
	 */
	public JobQueueBuilder withAmountOfThreads(int amountOfThreads) {
		queueOptionsBuilder.withAmountOfThreads(amountOfThreads);
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
