package com.frejdh.util.job.model.callables;

@FunctionalInterface
public interface JobAction {
	void action() throws Throwable;
}
