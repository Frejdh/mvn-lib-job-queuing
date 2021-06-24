package com.frejdh.util.job;

import com.frejdh.util.job.model.JobOptions;
import com.frejdh.util.job.model.JobStatus;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Builder(toBuilder = true, setterPrefix = "with")
public class Job {

	private final long addedTimestamp;

	@NotNull
	private final JobFunction jobFunction;

	@NotNull
	private final String resourceKey;

	@NotNull
	private final JobOptions jobOptions;

	private long jobId;

	private String description;

	public Job(@NotNull JobFunction jobFunction, @Nullable String resourceKey, @Nullable JobOptions jobOptions) {
		this.addedTimestamp = Instant.now().toEpochMilli();
		this.jobFunction = jobFunction;
		this.jobFunction.setJob(this);
		this.resourceKey = resourceKey != null ? resourceKey : UUID.randomUUID().toString();
		this.jobOptions = jobOptions != null ? jobOptions : JobOptions.builder().build();
	}

	public Job(@NotNull JobFunction jobFunction, @Nullable String resourceKey) {
		this(jobFunction, resourceKey, null);
	}

	public Job(@NotNull JobFunction jobFunction, @Nullable JobOptions jobOptions) {
		this(jobFunction, null, jobOptions);
	}

	public Job(@NotNull JobFunction jobFunction) {
		this(jobFunction, null, null);
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

	protected void setStatus(JobStatus status) {
		jobFunction.setStatus(status);
	}

	/**
	 * Get the thrown exception (if any)
	 * @return The throwable or null
	 */
	public Throwable getThrowable() {
		return jobFunction.getThrowable();
	}

	public boolean hasThrowable() {
		return jobFunction.hasThrowable();
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JobOptions getJobOptions() {
		return jobOptions;
	}

	public long getAddedTimestamp() {
		return addedTimestamp;
	}

	public static class JobBuilder {

		public JobBuilder withStatus(JobStatus status) {
			jobFunction.setStatus(status);
			return this;
		}
	}

	public void start() {
		jobFunction.start();
	}

}
