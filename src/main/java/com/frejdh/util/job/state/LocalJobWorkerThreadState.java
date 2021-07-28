package com.frejdh.util.job.state;

import com.frejdh.util.job.Job;
import lombok.Builder;
import java.util.concurrent.Future;

@Builder(toBuilder = true, setterPrefix = "with")
public class LocalJobWorkerThreadState {
	public Future<?> jobExecutionFuture;
}
