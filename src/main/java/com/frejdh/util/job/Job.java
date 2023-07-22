package com.frejdh.util.job;

import com.frejdh.util.job.exceptions.InvalidJobStateException;
import com.frejdh.util.job.model.JobOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.callables.JobAction;
import com.frejdh.util.job.model.callables.JobOnCallback;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.model.callables.JobOnFinalize;
import com.frejdh.util.job.model.callables.JobOnIdSet;
import com.frejdh.util.job.model.callables.JobOnStatusChange;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("FieldMayBeFinal")
public class Job {

	protected static final int UNASSIGNED_VALUE = -999999;

	@Builder.Default
	protected long addedTimestamp = UNASSIGNED_VALUE;

	@NotNull
	protected JobFunction jobFunction;

	protected String resourceKey;

	protected JobOptions jobOptions;

	protected JobOnIdSet onJobIdSetCallback;

	/**
	 * ID of the job. Will be overridden by the JobQueue implementation!
	 */
	@Builder.Default
	protected long jobId = UNASSIGNED_VALUE;

	/**
	 * Internal variable. Not intended for public/normal use.
	 */
	protected long previousJobId = UNASSIGNED_VALUE;

	protected String description;

	@Builder(setterPrefix = "with")
	public Job(@NotNull JobFunction jobFunction, long jobId, String resourceKey, JobOptions jobOptions, String description) {
		this.jobId = jobId;
		this.jobFunction = jobFunction;
		this.resourceKey = StringUtils.isNotBlank(resourceKey) ? resourceKey : null;
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

	Long previousJobId() {
		return previousJobId;
	}

	/**
	 * For setting job ID retroactively. Can only be used when the job is in the
	 * {@link JobStatus#WAITING_FOR_ID} state.
	 * @param newJobId Job ID to be used. Must be unique!
	 * @throws InvalidJobStateException If the state isn't {@link JobStatus#WAITING_FOR_ID}.
	 */
	public void setJobId(long newJobId) throws InvalidJobStateException {
		boolean isWaitingForId = getStatus().isWaitingForId();

		if ((isWaitingForId && newJobId != UNASSIGNED_VALUE) || (!isWaitingForId && this.jobId == UNASSIGNED_VALUE)) {
			internalSetJobId(newJobId);
		}
		else if (isWaitingForId) {
			throw new InvalidJobStateException(
					"Cannot set custom job ID. The job ID [" + UNASSIGNED_VALUE + "] is reserved for unassigned IDs"
			);
		}
		else {
			throw new InvalidJobStateException(
					"Cannot set custom job ID. JobStatus is currently: '" + jobFunction.getStatus()
					+ "' but needs to be '" + JobStatus.WAITING_FOR_ID + "'. Was the job builder called with "
					+ "setJobIdAfterCreation() ?"
			);
		}
	}

	/**
	 * Internal method, not for public/normal use. Meant to be overridden.
	 * Positive numbers only
	 */
	private void internalSetJobId(Long newJobId) {
		if (newJobId != null && newJobId >= 0) {
			this.previousJobId = jobId;
			this.jobId = newJobId;
			if (onJobIdSetCallback != null) {
				onJobIdSetCallback.onJobIdChange(this);
			}
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

	public void appendOnJobCallback(@NonNull JobOnCallback onCallback) {
		jobFunction.addOnCallback(onCallback);
	}

	void prependOnJobError(@NonNull JobOnError onError) {
		jobFunction.addOnJobError(onError, true);
	}

	public void appendOnJobError(@NonNull JobOnError onError) {
		jobFunction.addOnJobError(onError, false);
	}

	public void appendOnJobFinalize(@NonNull JobOnFinalize onFinalize) {
		jobFunction.addOnJobFinalize(onFinalize);
	}

	public void appendOnJobStatusChange(@NonNull JobOnStatusChange onStatusChange) {
		jobFunction.addOnStatusChange(onStatusChange);
	}

	public boolean hasStartedAlready() {
		return jobFunction.hasStartedAlready();
	}

	void setOnJobIdSetCallback(JobOnIdSet onJobIdSetCallback) {
		this.onJobIdSetCallback = onJobIdSetCallback;
	}

	@SuppressWarnings("FieldCanBeLocal")
	public static class JobBuilder {
		private static final JobAction ACTION_PLACEHOLDER = (jobRef) -> {};
		private long jobId = UNASSIGNED_VALUE;
		private JobFunction jobFunction = JobFunction.builder()
				.action(ACTION_PLACEHOLDER)
				.build();

		/**
		 * OPTIONAL. Only set if you intend to keep track of the job IDs by yourself.
		 */
		public JobBuilder withJobId(long jobId) {
			this.jobId = jobId;
			return this;
		}

		public JobBuilder withStatus(JobStatus status) {
			jobFunction.setStatus(status);
			return this;
		}

		public JobBuilder withAction(JobAction action) {
			jobFunction.setAction(action);
			return this;
		}

		public JobBuilder onCallback(JobOnCallback onCallback) {
			jobFunction.addOnCallback(onCallback);
			return this;
		}

		public JobBuilder onError(JobOnError onError) {
			jobFunction.addOnJobError(onError, false);
			return this;
		}

		public JobBuilder onFinalize(JobOnFinalize onFinalize) {
			jobFunction.addOnJobFinalize(onFinalize);
			return this;
		}

		public JobBuilder onStatusChange(JobOnStatusChange onStatusChange) {
			jobFunction.addOnStatusChange(onStatusChange);
			return this;
		}

		/**
		 * OPTIONAL. Only set if you intend to keep track of the job IDs by yourself, and you want the job to be
		 * created before setting the job ID. Call {@link Job#setJobId(long)}} to set the ID, or it will never start.
		 */
		public JobBuilder setJobIdAfterBuild() {
			jobFunction.setStatusWithoutCallback(JobStatus.WAITING_FOR_ID);
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
