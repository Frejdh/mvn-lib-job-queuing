package com.frejdh.util.job;

import com.frejdh.util.job.model.JobOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.callables.JobAction;
import com.frejdh.util.job.model.callables.JobOnCallback;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.model.callables.JobOnFinalize;
import com.frejdh.util.job.model.callables.JobOnStatusChange;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("FieldMayBeFinal")
public class Job {

	protected static final int UNASSIGNED_VALUE = -1;

	@Builder.Default
	protected long addedTimestamp = UNASSIGNED_VALUE;

	@NotNull
	protected JobFunction jobFunction;

	protected String resourceKey;

	protected JobOptions jobOptions;

	/**
	 * ID of the job. Will be overridden by the JobQueue implementation!
	 */
	@Builder.Default
	protected long jobId = UNASSIGNED_VALUE;

	protected String description;

	@Builder(setterPrefix = "with")
	public Job(@NotNull JobFunction jobFunction, String resourceKey, JobOptions jobOptions, String description) {
		this.jobFunction = jobFunction;
		this.resourceKey = resourceKey;
		this.jobOptions = jobOptions != null ? jobOptions : JobOptions.builder().build();
		this.description = description;
		setRequiredJobFunctionData();
	}

	private void setRequiredJobFunctionData() {
		this.jobFunction.setJob(this);
	}

	public String getResourceKey() {
		return resourceKey;
	}


	public long getJobId() {
		return jobId;
	}

	/**
	 * Positive numbers only
	 */
	void setJobId(Long id) {
		if (id != null && id >= 0) {
			this.jobId = id;
		}
	}

	public boolean hasJobId() {
		return this.jobId != UNASSIGNED_VALUE;
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

	void setStatus(JobStatus status) {
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

	void setOnJobStatusChange(JobOnStatusChange onStatusChange) {
		this.jobFunction.setOnStatusChange(onStatusChange);
	}

	JobOnError getOnJobError() {
		return this.jobFunction.getJobOnError();
	}

	void setOnJobError(JobOnError onError) {
		this.jobFunction.setOnError(onError);
	}

	public boolean hasStartedAlready() {
		return jobFunction.hasStartedAlready();
	}

	public static class JobBuilder {
		private static final JobAction ACTION_PLACEHOLDER = () -> {};
		private JobFunction jobFunction = JobFunction.builder()
				.action(ACTION_PLACEHOLDER)
				.build();

		public JobBuilder withStatus(JobStatus status) {
			jobFunction.setStatus(status);
			return this;
		}

		public JobBuilder withAction(JobAction action) {
			jobFunction.setAction(action);
			return this;
		}

		public JobBuilder onCallback(JobOnCallback onCallback) {
			jobFunction.setOnCallback(onCallback);
			return this;
		}

		public JobBuilder onError(JobOnError onError) {
			jobFunction.setOnError(onError);
			return this;
		}

		public JobBuilder onFinalize(JobOnFinalize onFinalize) {
			jobFunction.setOnFinalize(onFinalize);
			return this;
		}

		public JobBuilder onStatusChange(JobOnStatusChange onStatusChange) {
			jobFunction.setOnStatusChange(onStatusChange);
			return this;
		}

	}

	public void start() {
		jobFunction.start();
	}

	@Override
	public String toString() {
		return "Job{" +
				"addedTimestamp=" + addedTimestamp +
				", jobFunction=" + jobFunction +
				", resourceKey='" + resourceKey + '\'' +
				", jobOptions=" + jobOptions +
				", jobId=" + jobId +
				", description='" + description + '\'' +
				'}';
	}
}
