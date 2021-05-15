package com.frejdh.util.job.exceptions;

public class JobAlreadyStartedException extends RuntimeException {

	public JobAlreadyStartedException(String message) {
		super(message);
	}

	public JobAlreadyStartedException(String message, Throwable cause) {
		super(message, cause);
	}
}
