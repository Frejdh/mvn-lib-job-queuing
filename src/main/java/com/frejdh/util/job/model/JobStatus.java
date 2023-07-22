package com.frejdh.util.job.model;

public enum JobStatus {
	CREATED,	// Default value
	WAITING_FOR_ID,
	INITIALIZED,
	WAITING_FOR_RESOURCE,
	RUNNING_ACTION,
	RUNNING_CALLBACK,
	FINISHED,
	FAILED,
	CANCELED;

	public boolean isPending() {
		return this.equals(WAITING_FOR_ID)
			|| isPendingAndReady();
	}

	public boolean isPendingAndReady() {
		return this.equals(INITIALIZED)
			|| this.equals(WAITING_FOR_RESOURCE);
	}

	public boolean isWaitingForId() {
		return this.equals(WAITING_FOR_ID);
	}

	public boolean isRunning() {
		return this.equals(RUNNING_ACTION)
			|| this.equals(RUNNING_CALLBACK);
	}

	public boolean isFinished() {
		return this.equals(FINISHED)
			|| this.equals(FAILED)
			|| this.equals(CANCELED);
	}

}
