package com.frejdh.util.job.model.interfaces;

@FunctionalInterface
public interface JobError {
	void onError(Throwable throwable);
}
