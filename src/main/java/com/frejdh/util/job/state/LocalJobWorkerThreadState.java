package com.frejdh.util.job.state;

import lombok.Builder;
import java.util.concurrent.Future;


@Builder(toBuilder = true, setterPrefix = "with")
public class LocalJobWorkerThreadState {
	public Future<?> jobExecutionFuture;
}
