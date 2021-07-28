package com.frejdh.util.job.model;

import com.frejdh.util.job.Job;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Builder(toBuilder = true, setterPrefix = "with")
@Getter
public class QueueOptions {
	public static final int DEFAULT_MAX_PARALLEL_JOBS = 4;
	public static final int DEFAULT_AMOUNT_OF_THREADS = 1;

	@Builder.Default
	private int maxParallelJobs = DEFAULT_MAX_PARALLEL_JOBS;

	@Builder.Default
	private int amountOfThreads = DEFAULT_AMOUNT_OF_THREADS;

	private String persistenceFile;
	private boolean singleExecution;
	private List<Job> predefinedJobs;

	public boolean hasPersistenceFile() {
		return persistenceFile != null;
	}
}
