package com.frejdh.util.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frejdh.util.job.model.QueueOptions;
import java.io.File;
import java.io.IOException;
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

	public JobQueueBuilder withPredefinedJobs(List<Job> predefinedJobs) {
		this.predefinedJobs = predefinedJobs;
		return this;
	}

	public JobQueue build() {
		QueueOptions queueOptions = queueOptionsBuilder.build();
		return new JobQueue(queueOptions, getExistingJobs(queueOptions));
	}

	public JobQueue buildAndStart() {
		JobQueue retval = build();
		retval.start();
		return retval;
	}

	private List<Job> getExistingJobs(QueueOptions queueOptions) {
		if (predefinedJobs != null) {
			return predefinedJobs;
		}
		else if (!queueOptions.hasPersistenceFile()) {
			return null;
		}

		try {
			return new ObjectMapper().readValue(
					new File(queueOptions.getPersistenceFile()),
					new TypeReference<List<Job>>(){}
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JobQueueBuilder runOnceOnly() {
		queueOptionsBuilder.withSingleExecution(true);
		return this;
	}
}
