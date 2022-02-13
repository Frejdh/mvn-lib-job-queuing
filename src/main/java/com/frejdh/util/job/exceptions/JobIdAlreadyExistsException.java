package com.frejdh.util.job.exceptions;

public class JobIdAlreadyExistsException extends RuntimeException {

	public JobIdAlreadyExistsException(String message) {
		super(message);
	}

	public JobIdAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
