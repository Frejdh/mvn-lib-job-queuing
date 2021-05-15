package com.frejdh.util.job.model.interfaces;

@FunctionalInterface
public interface JobAction {
	void action() throws Throwable;
}
