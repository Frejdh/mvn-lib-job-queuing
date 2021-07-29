package com.frejdh.util.job;

import com.frejdh.util.job.model.JobOptions;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.callables.JobAction;
import com.frejdh.util.job.model.callables.JobOnCallback;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.model.callables.JobOnFinalize;
import com.frejdh.util.job.model.callables.JobOnStatusChange;
import com.pushtorefresh.javac_warning_annotation.Warning;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;


@SuppressWarnings("FieldMayBeFinal")
public class Job {

	private static final int UNASSIGNED_VALUE = -1;

	@Builder.Default
	private long addedTimestamp = UNASSIGNED_VALUE;

	@NotNull
	private JobFunction jobFunction;

	private final String resourceKey;

	@NotNull
	private final JobOptions jobOptions;

	/**
	 * ID of the job. Will be overridden by the JobQueue implementation!
	 */
	@Builder.Default
	private long jobId = UNASSIGNED_VALUE;

	private String description;

	/**
	 * Lombok toBuilder constructor. Not for normal usage.
	 */
	protected Job(long addedTimestamp,
				  @NotNull JobFunction jobFunction,
				  String resourceKey,
				  JobOptions jobOptions,
				  long jobId,
				  String description) {
		this(jobFunction, resourceKey, jobOptions, description, jobId);

		if (this.addedTimestamp == UNASSIGNED_VALUE && addedTimestamp != 0) {
			this.addedTimestamp = Instant.now().toEpochMilli();
		}
		else if (this.addedTimestamp == UNASSIGNED_VALUE) {
			this.addedTimestamp = addedTimestamp;
		}

		setJobId(jobId);
		this.description = description;
	}

	@Builder(toBuilder = true, setterPrefix = "with")
	public Job(@NotNull JobFunction jobFunction, String resourceKey, JobOptions jobOptions, String description, long jobId) {
		this.jobFunction = jobFunction;
		this.resourceKey = resourceKey;
		this.jobOptions = jobOptions != null ? jobOptions : JobOptions.builder().build();
		this.description = description;
		this.jobId = jobId;
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

	void setOnJobStatusChange(JobOnStatusChange onStatusChange) {
		this.jobFunction.setOnStatusChange(onStatusChange);
	}

	JobOnError getOnJobError() {
		return this.jobFunction.getJobOnError();
	}

	void setOnJobError(JobOnError onError) {
		this.jobFunction.setOnError(onError);
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

		/**
		 * This will be overridden by the JobQueue's DAO implementation.
		 * This is only suitable to be called by this aforementioned DAO implementation.
		 */
		@Deprecated
		@SuppressWarnings("DeprecatedIsStillUsed")
		public JobBuilder withJobId(long jobId) {
			this.jobId = jobId;
			return this;
		}

	}

	public void start() {
		jobFunction.start();
	}

}
