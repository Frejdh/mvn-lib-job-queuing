package com.frejdh.util.job.state;

import com.frejdh.util.job.Job;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


public class LocalJobWorkerThreadState {
	public Future<?> jobExecutionFuture;
}
