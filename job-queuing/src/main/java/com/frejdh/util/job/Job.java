package com.frejdh.util.job;

import com.frejdh.util.job.model.JobStatus;
import org.jetbrains.annotations.NotNull;

public class Job {

	@NotNull
	private final JobFunction jobFunction;

	@NotNull
	private final String resourceKey;

	private long jobId;

	private String description;

	public Job(@NotNull JobFunction jobFunction, @NotNull String resourceKey) {
		this.jobFunction = jobFunction;
		this.jobFunction.setJob(this);
		this.resourceKey = resourceKey;
	}

	@NotNull
	public String getResourceKey() {
		return resourceKey;
	}


	public long getJobId() {
		return jobId;
	}

	void setJobId(Long id) {
		this.jobId = id;
	}

	/**
	 * Get job start time as epoch number
	 * @return The timestamp
	 */
	public Long getStartTime() {
		return jobFunction.getStartTime();
	}

	/**
	 * Get job stop time as epoch number
	 * @return The timestamp
	 */
	public Long getStopTime() {
		return jobFunction.getStopTime();
	}

	public boolean isStarted() {
		return getStartTime() != null;
	}

	public boolean isFinished() {
		return getStopTime() != null;
	}

	public boolean isRunning() {
		return isStarted() && !isFinished();
	}

	public String getDescription() {
		return description;
	}

	public JobStatus getStatus() {
		return jobFunction.getStatus();
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void start() {
		jobFunction.start();
	}

}
