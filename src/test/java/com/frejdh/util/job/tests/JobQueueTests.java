package com.frejdh.util.job.tests;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.JobFunction;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.JobQueueBuilder;
import com.frejdh.util.job.model.JobStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class JobQueueTests extends AbstractQueueTests {

	@Test
	public void doSimpleAction() {
		AtomicInteger fieldToChange = new AtomicInteger(0);
		int valueToChangeTo = 10;

		List<Job> jobs = Collections.singletonList(new Job(new JobFunction(() -> fieldToChange.set(valueToChangeTo)), "doSimpleAction"));

		queue = new JobQueueBuilder().runOnceOnly().withPredefinedJobs(jobs).buildAndStart();
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		Assert.assertEquals(valueToChangeTo, fieldToChange.get());
	}

	@Test
	public void ensureResourceIsLocked() throws Throwable {
		int originalValue = 10;
		int valueToChangeToFirst = 11;
		int valueToChangeToLater = 12;
		String resourceKey = "ensureResourceIsLocked";
		AtomicInteger fieldToChange = new AtomicInteger(originalValue);

		final Job jobLockResource = new Job(new JobFunction(() -> {
			Thread.sleep(1000);
			fieldToChange.set(valueToChangeToFirst);
		}), resourceKey);
		final Job jobWaitForResource = new Job(new JobFunction(() -> {
			Assert.assertEquals("Expected value to be changed before this: " + valueToChangeToFirst, valueToChangeToFirst, fieldToChange.get());
			fieldToChange.set(valueToChangeToLater);
		}), resourceKey);

		queue = new JobQueueBuilder().buildAndStart();
		queue.add(jobLockResource);
		queue.add(jobWaitForResource);

		getDaoService().getPendingJobs();


		Thread.sleep(200);
		Assert.assertEquals("Expected the locking job to have the the status " + JobStatus.RUNNING_ACTION, JobStatus.RUNNING_ACTION, jobLockResource.getStatus());
		Assert.assertEquals("Expected the waiting job to have the the status " + JobStatus.WAITING_FOR_RESOURCE, JobStatus.WAITING_FOR_RESOURCE, jobWaitForResource.getStatus());
		Assert.assertEquals("Expected value to be unchanged from the original: " + originalValue, originalValue, fieldToChange.get());
		Thread.sleep(2000);
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		Assert.assertEquals("Expected value to be finished with: " + valueToChangeToLater, valueToChangeToLater, fieldToChange.get());
		Assert.assertFalse("Expected no exceptions", jobWaitForResource.hasThrowable());

		Assert.assertEquals(0, getDaoService().getPendingJobs().size());
		Assert.assertEquals(0, getDaoService().getCurrentJobs().size());
		Assert.assertEquals(2, getDaoService().getFinishedJobs().size());

		System.out.println("pending: " + getDaoService().getPendingJobs());
		System.out.println("current: " + getDaoService().getCurrentJobs());
		System.out.println("finished: " + getDaoService().getFinishedJobs());

	}

	@Test
	public void canCatchExceptions() {
		final Job job = new Job(new JobFunction(() -> {
			throw new NullPointerException("test");
		}));

		queue = new JobQueueBuilder().runOnceOnly().withPredefinedJobs(Collections.singletonList(job)).buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);
		Assert.assertTrue(job.hasThrowable());
		Assert.assertEquals(NullPointerException.class, job.getThrowable().getClass());
		Assert.assertEquals(job.getThrowable().getMessage(), "test");
	}

}
