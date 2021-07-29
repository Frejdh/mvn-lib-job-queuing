package com.frejdh.util.job.model.callables;

@FunctionalInterface
public interface JobOnError {
	void onError(Throwable throwable);
}
