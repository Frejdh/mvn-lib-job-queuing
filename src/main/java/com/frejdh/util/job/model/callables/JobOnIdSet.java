package com.frejdh.util.job.model.callables;

import com.frejdh.util.job.Job;

@FunctionalInterface
public interface JobOnIdSet {
	void onJobIdChange(Job job);
}
