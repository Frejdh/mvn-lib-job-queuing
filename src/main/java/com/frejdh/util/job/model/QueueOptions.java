package com.frejdh.util.job.model;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.callables.JobOnError;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
@Builder(toBuilder = true, setterPrefix = "with")
@Getter
public class QueueOptions {
	public static final int DEFAULT_MAX_AMOUNT_OF_THREADS = 4;
	public static final boolean DEFAULT_USE_CACHED_THREAD_POOL = true;

	/**
	 * Max amount of threads that can be created.
	 * If fixed thread pool (non-cached) according to {@link #isCachedThreadPool()},
	 * started threads will always be the maximum value.
	 * Default value is: {@link #DEFAULT_MAX_AMOUNT_OF_THREADS}
	 */
	@Builder.Default
	private int maxAmountOfThreads = DEFAULT_MAX_AMOUNT_OF_THREADS;

	private String persistenceFile;

	@Builder.Default
	private boolean singleExecution = false;

	@Builder.Default
	private List<Job> predefinedJobs = new ArrayList<>();

	private JobOnError onJobError;

	private boolean debugMode;

	/**
	 * Determines if the Job Queue should use a cached thread pool by default.
	 * In other words, threads are started when needed, or reused depending on the situation.
	 * Recommended for queue's that doesn't require all threads to be active at all times.
	 * Default value is: {@link #DEFAULT_USE_CACHED_THREAD_POOL}
	 */
	@Builder.Default
	private boolean cachedThreadPool = DEFAULT_USE_CACHED_THREAD_POOL;

	public static QueueOptions getDefault() {
		return QueueOptions.builder().build();
	}
}
