package com.frejdh.util.job.tests;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.JobQueueBuilder;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class JobQueueTests extends AbstractQueueTests {

	/**
	 * Default queue builder with error handling. Don't use if errors are expected.
	 *
	 * @return A JobQueueBuilder instance
	 */
	private static JobQueueBuilder defaultJobQueue() {
		return new JobQueueBuilder()
				.withDebugMode(true)
				.withOnErrorHandler(Throwable::printStackTrace);
	}

	@Theory
	public void doSimpleAction() {
		AtomicInteger fieldToChange = new AtomicInteger(0);
		int valueToChangeTo = 10;

		List<Job> jobs = Collections.singletonList(Job.builder()
				.withAction(() -> fieldToChange.set(valueToChangeTo))
				.withResourceKey("doSimpleAction")
				.build());

		queue = defaultJobQueue().runOnceOnly().withPredefinedJobs(jobs).buildAndStart();
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		Assert.assertEquals(JobStatus.FINISHED, jobs.get(0).getStatus());
		Assert.assertEquals(valueToChangeTo, fieldToChange.get());
	}

	@Theory
	public void ensureResourceIsLocked() throws Throwable {
		int originalValue = 10;
		int valueToChangeToFirst = 11;
		int valueToChangeToLater = 12;
		String resourceKey = "ensureResourceIsLocked";
		AtomicInteger fieldToChange = new AtomicInteger(originalValue);

		Job jobLockResource = Job.builder()
				.withAction(() -> {
					Thread.sleep(600);
					fieldToChange.set(valueToChangeToFirst);
				})
				.withResourceKey(resourceKey)
				.build();
		Job jobWaitForResource = Job.builder()
				.withAction(() -> {
					Assert.assertEquals("Expected value to be changed before this: " + valueToChangeToFirst, valueToChangeToFirst, fieldToChange.get());
					fieldToChange.set(valueToChangeToLater);
				})
				.withResourceKey(resourceKey)
				.build();

		queue = defaultJobQueue().buildAndStart();
		queue.add(jobLockResource);
		queue.add(jobWaitForResource);


		Thread.sleep(200);
		Assert.assertEquals("Expected the locking job to have the the status " + JobStatus.RUNNING_ACTION, JobStatus.RUNNING_ACTION, jobLockResource.getStatus());
		Assert.assertEquals("Expected the waiting job to have the the status " + JobStatus.WAITING_FOR_RESOURCE, JobStatus.WAITING_FOR_RESOURCE, jobWaitForResource.getStatus());
		Assert.assertEquals("Expected value to be unchanged from the original: " + originalValue, originalValue, fieldToChange.get());
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		Assert.assertEquals("Expected value to be finished with: " + valueToChangeToLater, valueToChangeToLater, fieldToChange.get());
		Assert.assertFalse("Expected no exceptions", jobWaitForResource.hasThrowable());

		Assert.assertEquals(0, getDaoService().getPendingJobs().size());
		Assert.assertEquals(0, getDaoService().getCurrentJobs().size());
		Assert.assertEquals(2, getDaoService().getFinishedJobs().size());
	}

	@Theory
	public void canCatchExceptions() {
		final Job job = Job.builder()
				.withAction(() -> {
					throw new NullPointerException("test");
				})
				.onError((throwable) -> {
					LOGGER.info("Caught exception successfully!");
				})
				.build();

		queue = defaultJobQueue()
				.runOnceOnly()
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);
		Assert.assertTrue(job.hasThrowable());
		Assert.assertEquals(NullPointerException.class, job.getThrowable().getClass());
		Assert.assertEquals(job.getThrowable().getMessage(), "test");
	}

}
