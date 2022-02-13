package com.frejdh.util.job.exceptions;

public class InvalidJobStatusForNewJobIdException extends RuntimeException {

	public InvalidJobStatusForNewJobIdException(String message) {
		super(message);
	}

	public InvalidJobStatusForNewJobIdException(String message, Throwable cause) {
		super(message, cause);
	}
}
