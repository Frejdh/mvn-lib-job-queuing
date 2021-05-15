package com.frejdh.util.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frejdh.util.job.model.JobOptions;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JobQueueBuilder {

	private final JobOptions options;
	private List<Job> predefinedJobs;

	public JobQueueBuilder() {
		options = new JobOptions();
	}

	public static JobQueueBuilder getInstance() {
		return new JobQueueBuilder();
	}

	/**
	 * Set the max number of parallel jobs per thread.
	 * A value of 4 jobs on 2 threads, means that it can be up to 8 <i>(4*2)</i> concurrent jobs at any time.
	 * <strong>Default value is {@link JobOptions#DEFAULT_MAX_PARALLEL_JOBS}</strong>.
	 * @return This builder reference.
	 */
	public JobQueueBuilder setMaxParallelJobs(int maxParallelJobs) {
		options.setMaxParallelJobs(maxParallelJobs);
		return this;
	}

	/**
	 * Set the number of threads to utilize.
	 * <strong>Default value is {@link JobOptions#DEFAULT_AMOUNT_OF_THREADS}</strong>.
	 * @return This builder reference.
	 */
	public JobQueueBuilder setAmountOfThreads(int amountOfThreads) {
		options.setAmountOfThreads(amountOfThreads);
		return this;
	}

	public JobQueueBuilder setPredefinedJobs(List<Job> predefinedJobs) {
		this.predefinedJobs = predefinedJobs;
		return this;
	}

	public JobQueue build() {
		return new JobQueue(options, getExistingJobs());
	}

	public JobQueue buildAndStart() {
		JobQueue retval = build();
		retval.start();
		return retval;
	}

	private List<Job> getExistingJobs() {
		if (predefinedJobs != null) {
			return predefinedJobs;
		}
		else if (!options.hasPersistenceFile()) {
			return null;
		}

		try {
			return new ObjectMapper().readValue(
					new File(options.getPersistenceFile()),
					new TypeReference<List<Job>>(){}
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public JobQueueBuilder runOnceOnly() {
		options.runOnce();
		return this;
	}
}
