package com.frejdh.util.job.model.callables;

@FunctionalInterface
public interface JobOnFinalize {
	void onComplete();
}
