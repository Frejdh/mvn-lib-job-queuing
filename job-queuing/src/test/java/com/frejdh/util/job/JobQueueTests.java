package com.frejdh.util.job;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class JobQueueTests {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Test
	public void doSimpleAction() {
		AtomicInteger fieldToChange = new AtomicInteger(0);
		int valueToChangeTo = 10;

		List<Job> jobs = Collections.singletonList(new Job(new JobFunction(() -> fieldToChange.set(valueToChangeTo)), "doSimpleAction"));

		JobQueue queue = new JobQueueBuilder().runOnceOnly().setPredefinedJobs(jobs).buildAndStart();
		queue.stopAndAwait(5000, TimeUnit.SECONDS);
		Assert.assertEquals(valueToChangeTo, fieldToChange.get());
	}
}
