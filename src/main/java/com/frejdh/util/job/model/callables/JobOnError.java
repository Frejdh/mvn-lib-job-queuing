package com.frejdh.util.job.model.callables;

import com.frejdh.util.job.Job;

@FunctionalInterface
public interface JobOnError {
	void onError(Job job, Throwable throwable);
}
