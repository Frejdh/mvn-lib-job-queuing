package com.frejdh.util.job.model.interfaces;

import com.frejdh.util.job.Job;

@FunctionalInterface
public interface JobCallback {
	void callback(Job job);
}
