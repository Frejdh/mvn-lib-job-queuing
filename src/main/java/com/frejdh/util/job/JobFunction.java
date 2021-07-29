package com.frejdh.util.job;

import com.frejdh.util.job.exceptions.JobAlreadyStartedException;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.callables.JobAction;
import com.frejdh.util.job.model.callables.JobOnCallback;
import com.frejdh.util.job.model.callables.JobOnError;
import com.frejdh.util.job.model.callables.JobOnFinalize;
import com.frejdh.util.job.model.callables.JobOnStatusChange;
import lombok.Builder;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Model class for the job operation and it's properties.
 */
public class JobFunction {

	private Job job;

	@NonNull private JobAction action;
	private JobOnCallback onJobCallback;
	private JobOnError onJobError;
	private JobOnFinalize onJobFinalize;
	private JobOnStatusChange onStatusChange;
	private JobStatus status;
	private Long startTime;
	private Long stopTime;
	private Throwable throwable;

	/**
	 * Lombok builder alternative (only uses fields in constructor)
	 */
	@Builder
	protected JobFunction(@NonNull JobAction action,
						  JobOnCallback onJobCallback,
						  JobOnError onJobError,
						  JobOnFinalize onJobFinalize,
						  JobOnStatusChange onStatusChange) {
		this.action = action;
		this.onJobCallback = onJobCallback;
		this.onJobError = onJobError;
		this.onJobFinalize = onJobFinalize;
		this.onStatusChange = onStatusChange;
	}

	public JobFunction(@NonNull JobAction action) {
		this.action = action;
		this.status = JobStatus.INITIALIZED;
	}

	public JobFunction(@NonNull JobAction action,
					   @Nullable JobOnCallback onSuccess,
					   @Nullable JobOnError onError,
					   @Nullable JobOnFinalize onComplete) {
		this(action);
		this.onJobCallback = onSuccess;
		this.onJobError = onError;
		this.onJobFinalize = onComplete;
	}

	JobFunction setJob(Job job) {
		this.job = job;
		return this;
	}

	void setStatus(JobStatus status) {
		final boolean isStatusChanged = this.status == null || !this.status.equals(status);
		this.status = status;
		if (onStatusChange != null && isStatusChanged) {
			onStatusChange.onStatusChange();
		}
	}

	void setAction(@NotNull JobAction jobAction) {
		this.action = jobAction;
	}

	void setOnCallback(JobOnCallback jobCallback) {
		this.onJobCallback = jobCallback;
	}

	void setOnError(JobOnError jobError) {
		this.onJobError = jobError;
	}

	void setOnFinalize(JobOnFinalize jobFinalize) {
		this.onJobFinalize = jobFinalize;
	}

	void setOnStatusChange(JobOnStatusChange onStatusChange) {
		this.onStatusChange = onStatusChange;
	}

	JobOnError getJobOnError() {
		return this.onJobError;
	}

	public Job getJob() {
		return job;
	}

	public JobStatus getStatus() {
		return status;
	}

	public Long getStartTime() {
		return startTime;
	}

	public Long getStopTime() {
		return stopTime;
	}



	/**
	 * Start the job function (and callback)
	 */
	public void start() throws JobAlreadyStartedException {
		checkIfStartedAlready();
		synchronized (this) {
			checkIfStartedAlready();
			this.startTime = Instant.now().toEpochMilli();
			try {
				setStatus(JobStatus.RUNNING_ACTION);
				action.action();

				if (onJobCallback != null) {
					setStatus(JobStatus.RUNNING_CALLBACK);
					onJobCallback.callback(job);
				}

				setStatus(JobStatus.FINISHED);
			} catch (Throwable throwable) {
				setStatus(JobStatus.FAILED);
				this.throwable = throwable;
				if (onJobError != null) {
					onJobError.onError(throwable);
				}
			} finally {
				stopTime = Instant.now().toEpochMilli();
				if (onJobFinalize != null) {
					try {
						onJobFinalize.onComplete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void checkIfStartedAlready() throws JobAlreadyStartedException {
		boolean isStartedAlready = (startTime != null);
		if (isStartedAlready) {
			throw new JobAlreadyStartedException("Job already started at timestamp: " + startTime + ". Currently (" + Instant.now().toEpochMilli() + ")");
		}
	}

	public boolean hasThrowable() {
		return throwable != null;
	}

	/**
	 * Get the thrown throwable/exception (if any)
	 * @return The throwable or null
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return "JobFunction{" +
				"job=" + job +
				", status=" + status +
				", startTime=" + startTime +
				", stopTime=" + stopTime +
				'}';
	}
}
