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
import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * Model class for the job operation and it's properties.
 */
public class JobFunction {

	private Job job;

	@NonNull private JobAction action;
	private List<JobOnCallback> onJobCallbacks;
	private List<JobOnError> onJobErrors;
	private List<JobOnFinalize> onJobFinalizes;
	private List<JobOnStatusChange> onStatusChanges;
	private JobStatus status;
	private Long startTime;
	private Long stopTime;
	private Throwable throwable;

	/**
	 * Lombok builder alternative (only uses fields in constructor)
	 */
	@Builder
	protected JobFunction(@NonNull JobAction action,
						  @Singular List<JobOnCallback> onJobCallbacks,
						  @Singular List<JobOnError> onJobErrors,
						  @Singular List<JobOnFinalize> onJobFinalizes,
						  @Singular List<JobOnStatusChange> onStatusChanges) {
		this.action = action;
		this.onJobCallbacks = new LinkedList<>(onJobCallbacks);
		this.onJobErrors = new LinkedList<>(onJobErrors);
		this.onJobFinalizes = new LinkedList<>(onJobFinalizes);
		this.onStatusChanges = new LinkedList<>(onStatusChanges);
	}

	public JobFunction setJob(Job job) {
		this.job = job;
		return this;
	}

	public void setStatus(JobStatus status) {
		final boolean isStatusChanged = this.status == null || !this.status.equals(status);
		this.status = status;
		if (onStatusChanges != null && isStatusChanged) {
			onStatusChanges.forEach(onStatusChange -> onStatusChange.onStatusChange(job));
		}
	}

	public void setAction(@NotNull JobAction jobAction) {
		this.action = jobAction;
	}

	public List<JobOnCallback> getOnJobCallbacks() {
		return onJobCallbacks;
	}

	public void addOnCallback(@NotNull JobOnCallback jobCallback) {
		this.onJobCallbacks.add(jobCallback);
	}

	public void addOnJobError(@NotNull JobOnError jobError, boolean prepend) {
		if (prepend) {
			this.onJobErrors.add(0, jobError);
		}
		else {
			this.onJobErrors.add(jobError);
		}
	}

	public void addOnJobFinalize(@NotNull JobOnFinalize jobFinalize) {
		this.onJobFinalizes.add(jobFinalize);
	}

	public void addOnStatusChange(@NotNull JobOnStatusChange onStatusChange) {
		this.onStatusChanges.add(onStatusChange);
	}

	public List<JobOnError> getOnJobErrors() {
		return this.onJobErrors;
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
		throwIfStartedAlready();
		synchronized (this) {
			throwIfStartedAlready();
			this.startTime = Instant.now().toEpochMilli();
			try {
				setStatus(JobStatus.RUNNING_ACTION);
				action.action();

				if (onJobCallbacks != null) {
					setStatus(JobStatus.RUNNING_CALLBACK);
					onJobCallbacks.forEach(callback -> callback.callback(job));
				}

				setStatus(JobStatus.FINISHED);
			} catch (Throwable throwable) {
				this.throwable = throwable;
				setStatus(JobStatus.FAILED);
				try {
					if (onJobErrors != null) {
						onJobErrors.forEach(onError -> onError.onError(job, throwable));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} finally {
				stopTime = Instant.now().toEpochMilli();
				if (onJobFinalizes != null) {
					try {
						onJobFinalizes.forEach(onFinalize -> onFinalize.onComplete(job));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public boolean hasStartedAlready() {
		return (startTime != null);
	}

	private void throwIfStartedAlready() throws JobAlreadyStartedException {
		boolean isStartedAlready = hasStartedAlready();
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
				"jobId=" + (job != null ? job.getJobId() : null) +
				", status=" + status +
				", startTime=" + startTime +
				", stopTime=" + stopTime +
				'}';
	}
}
