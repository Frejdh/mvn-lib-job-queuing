package com.frejdh.util.job.model;

public enum JobStatus {
	INITIALIZED,
	ADDED_TO_QUEUE,
	WAITING_FOR_RESOURCE,
	RUNNING_ACTION,
	RUNNING_CALLBACK,
	FINISHED,
	FAILED
}
