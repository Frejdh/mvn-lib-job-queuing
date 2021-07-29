package com.frejdh.util.job.model;

public enum JobStatus {
	INITIALIZED,
	ADDED_TO_QUEUE,
	WAITING_FOR_RESOURCE,
	RUNNING_ACTION,
	RUNNING_CALLBACK,
	FINISHED,
	FAILED,
	CANCELED;

	public boolean isPending() {
		return this.equals(INITIALIZED)
			|| this.equals(ADDED_TO_QUEUE)
			|| this.equals(WAITING_FOR_RESOURCE);
	}

	public boolean isRunning() {
		return this.equals(RUNNING_ACTION)
				|| this.equals(RUNNING_CALLBACK);
	}

	public boolean isDone() {
		return this.equals(FINISHED)
			|| this.equals(FAILED)
			|| this.equals(CANCELED);
	}

}
