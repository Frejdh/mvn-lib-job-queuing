package com.frejdh.util.job.model;

public enum JobStatus {
	UNINITIALIZED,
	ADDED,
	WAITING,
	RUNNING_ACTION,
	RUNNING_CALLBACK,
	FINALIZING,
	FINISHED,
	FAILED
}
