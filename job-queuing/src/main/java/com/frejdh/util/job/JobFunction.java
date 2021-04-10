package com.frejdh.util.job;

import com.frejdh.util.job.exceptions.JobAlreadyStartedException;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.model.interfaces.JobAction;
import com.frejdh.util.job.model.interfaces.JobCallback;
import com.frejdh.util.job.model.interfaces.JobError;
import com.frejdh.util.job.model.interfaces.JobFinalize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.Instant;

/**
 * Model class for the job operation and it's properties.
 */
public class JobFunction {

	private Job job;
	private final JobAction jobAction;
	private JobCallback jobCallback;
	private JobError jobError;
	private JobFinalize jobFinalize;
	private JobStatus status;
	private Long startTime;
	private Long stopTime;

	public JobFunction(JobAction jobAction) {
		this.jobAction = jobAction;
	}

	public JobFunction(@NotNull JobAction action,
					   @Nullable JobCallback onSuccess,
					   @Nullable JobError onError,
					   @Nullable JobFinalize onComplete) {
		this.jobAction = action;
		this.jobCallback = onSuccess;
		this.jobError = onError;
		this.jobFinalize = onComplete;
		this.status = JobStatus.ADDED;
	}

	JobFunction setJob(Job job) {
		this.job = job;
		return this;
	}

	public JobFunction setCallback(JobCallback jobCallback) {
		this.jobCallback = jobCallback;
		return this;
	}

	public JobFunction setOnError(JobError jobError) {
		this.jobError = jobError;
		return this;
	}

	public JobFunction setFinalize(JobFinalize jobFinalize) {
		this.jobFinalize = jobFinalize;
		return this;
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
		synchronized (this) {
			checkIfStartedAlready();
			this.startTime = Instant.now().toEpochMilli();
			try {
				status = JobStatus.RUNNING_ACTION;
				jobAction.action();

				if (jobCallback != null) {
					status = JobStatus.RUNNING_CALLBACK;
					jobCallback.callback(job);
				}

				status = JobStatus.FINISHED;
			} catch (Throwable throwable) {
				status = JobStatus.FAILED;
				if (jobError != null) {
					jobError.onError(throwable);
				}
				else {
					throw throwable;
				}
			} finally {
				stopTime = Instant.now().toEpochMilli();
				if (jobFinalize != null) {
					try {
						jobFinalize.onComplete();
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
