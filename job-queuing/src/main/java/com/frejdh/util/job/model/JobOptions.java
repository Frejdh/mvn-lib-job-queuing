package com.frejdh.util.job.model;

import com.frejdh.util.job.Job;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class JobOptions {
	public static final int DEFAULT_MAX_PARALLEL_JOBS = 4;
	public static final int DEFAULT_AMOUNT_OF_THREADS = 1;

	private int maxParallelJobs = DEFAULT_MAX_PARALLEL_JOBS;
	private int amountOfThreads = DEFAULT_AMOUNT_OF_THREADS;
	private String persistenceFile;
	private boolean isRunningOnce;
	private List<Job> predefinedJobs;

	public int getMaxParallelJobs() {
		return maxParallelJobs;
	}

	public int getAmountOfThreads() {
		return amountOfThreads;
	}

	public String getPersistenceFile() {
		return persistenceFile;
	}

	public boolean hasPersistenceFile() {
		return persistenceFile != null;
	}

	public void setMaxParallelJobs(int maxParallelJobs) {
		this.maxParallelJobs = maxParallelJobs;
	}

	public void setAmountOfThreads(int amountOfThreads) {
		this.amountOfThreads = amountOfThreads;
	}

	public void setPersistenceFile(@Nullable String persistenceFile) {
		this.persistenceFile = persistenceFile;
	}

	public void setPredefinedJobs(List<Job> jobs) {
		this.predefinedJobs = jobs;
	}

	/**
	 * If no background thread should be used. Combine this with {@link #setPredefinedJobs(List)}.
	 */
	public void runOnce() {
		this.isRunningOnce = true;
	}

	public boolean isRunningOnce() {
		return isRunningOnce;
	}
}
