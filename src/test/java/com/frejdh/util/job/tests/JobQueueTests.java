package com.frejdh.util.job.tests;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.JobFunction;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.JobQueueBuilder;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import org.junit.After;
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


@RunWith(Theories.class) // To run multiple DAO configurations with the same tests
public class JobQueueTests {

	@DataPoints
	public static final List<DaoPersistenceMode> PERSISTENCE_MODES = getPersistenceModes();
	private static final Logger LOGGER = Logger.getLogger(JobQueueTests.class.getCanonicalName());
	private JobQueue queue;

	private static List<DaoPersistenceMode> getPersistenceModes() {
		return Arrays.stream(DaoPersistenceMode.values())
				.filter(mode -> !mode.equals(DaoPersistenceMode.CUSTOM))
				.collect(Collectors.toList());
	}

	@After
	public void afterTests() {
		if (queue != null) {
			queue.stopNow();
		}
	}

	/**
	 * Default queue builder with error handling. Don't use if errors are expected.
	 *
	 * @return A JobQueueBuilder instance
	 */
	private static JobQueueBuilder defaultJobQueue() {
		return new JobQueueBuilder().withOnErrorHandler(Throwable::printStackTrace);
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
					System.out.println("1: " + Thread.currentThread().getName());
					Thread.sleep(2000);
					// TODO: Fix InterruptedException
					fieldToChange.set(valueToChangeToFirst);
				})
				.withResourceKey(resourceKey)
				.build();
		Job jobWaitForResource = Job.builder()
				.withAction(() -> {
					System.out.println("2: " + Thread.currentThread().getName());
					Assert.assertEquals("Expected value to be changed before this: " + valueToChangeToFirst, valueToChangeToFirst, fieldToChange.get());
					fieldToChange.set(valueToChangeToLater);
				})
				.withResourceKey(resourceKey)
				.build();

		queue = defaultJobQueue().buildAndStart();
		jobLockResource = queue.add(jobLockResource);
		jobWaitForResource = queue.add(jobWaitForResource);

		Assert.assertEquals("Expected the locking job to have the the status " + JobStatus.RUNNING_ACTION, JobStatus.RUNNING_ACTION, jobLockResource.getStatus());
		Assert.assertEquals("Expected the waiting job to have the the status " + JobStatus.WAITING_FOR_RESOURCE, JobStatus.WAITING_FOR_RESOURCE, jobWaitForResource.getStatus());
		Assert.assertEquals("Expected value to be unchanged from the original: " + originalValue, originalValue, fieldToChange.get());
		queue.stopAndAwait(4000, TimeUnit.SECONDS);
		Assert.assertEquals("Expected value to be finished with: " + valueToChangeToLater, valueToChangeToLater, fieldToChange.get());
		Assert.assertFalse("Expected no exceptions", jobWaitForResource.hasThrowable());
	}

	@Theory
	public void canCatchExceptions() {
		final Job job = Job.builder()
				.withAction(() -> {
					throw new NullPointerException("test");
				}).build();

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
