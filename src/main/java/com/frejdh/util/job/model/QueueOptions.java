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
	public static final int DEFAULT_MAX_PARALLEL_JOBS = 4;
	public static final int DEFAULT_AMOUNT_OF_THREADS = 1;

	@Builder.Default
	private int maxParallelJobs = DEFAULT_MAX_PARALLEL_JOBS;

	@Builder.Default
	private int amountOfThreads = DEFAULT_AMOUNT_OF_THREADS;

	private String persistenceFile;

	@Builder.Default
	private boolean singleExecution = false;

	@Builder.Default
	private List<Job> predefinedJobs = new ArrayList<>();

	private JobOnError onJobError;

	private boolean debugMode;

	public static QueueOptions getDefault() {
		return QueueOptions.builder().build();
	}
}
