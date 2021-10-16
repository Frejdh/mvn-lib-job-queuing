package com.frejdh.util.job.model.callables;

import com.frejdh.util.job.Job;

@FunctionalInterface
public interface JobOnFinalize {
	void onComplete(Job job);
}
