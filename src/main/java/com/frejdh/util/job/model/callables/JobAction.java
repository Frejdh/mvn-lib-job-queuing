package com.frejdh.util.job.model.callables;

import com.frejdh.util.job.Job;

@FunctionalInterface
public interface JobAction {
	void action(Job jobRef) throws Throwable;
}
