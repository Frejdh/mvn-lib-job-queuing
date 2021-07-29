package com.frejdh.util.job.model.callables;

@FunctionalInterface
public interface JobOnStatusChange {
	void onStatusChange();
}
