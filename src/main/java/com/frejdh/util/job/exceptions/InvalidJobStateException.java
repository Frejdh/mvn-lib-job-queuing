package com.frejdh.util.job.exceptions;

public class InvalidJobStateException extends RuntimeException {

	public InvalidJobStateException(String message) {
		super(message);
	}

	public InvalidJobStateException(String message, Throwable cause) {
		super(message, cause);
	}
}
