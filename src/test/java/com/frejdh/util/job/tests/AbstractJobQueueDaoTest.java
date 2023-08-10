package com.frejdh.util.job.tests;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.JobQueueBuilder;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.JobQueueService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractJobQueueDaoTest {

	protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	protected final JobQueueService jobQueueService;
	protected JobQueue queue;

	public AbstractJobQueueDaoTest(JobQueueService jobQueueService) {
		this.jobQueueService = jobQueueService;
	}

	@BeforeEach
	public void beforeTests() {
		queue = null;
	}

	@AfterEach
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
	protected JobQueueBuilder defaultJobQueue() {
		return defaultJobQueue(true);
	}

	/**
	 * Default queue builder with error handling. Don't use if errors are expected.
	 *
	 * @return A JobQueueBuilder instance
	 */
	protected JobQueueBuilder defaultJobQueue(boolean withErrorHandler) {
		JobQueueBuilder queueBuilder = new JobQueueBuilder()
				.withDebugMode(true)
				.withCustomDaoService(jobQueueService);
		if (withErrorHandler) {
			queueBuilder.withOnErrorHandler((jobReference, throwable) -> throwable.printStackTrace());
		}

		return queueBuilder;
	}

	@Test
	public void doSimpleAction() {
		AtomicInteger fieldToChange = new AtomicInteger(0);
		int valueToChangeTo = 10;

		List<Job> jobs = Collections.singletonList(Job.builder()
				.withAction((jobRef) -> fieldToChange.set(valueToChangeTo))
				.withResourceKey("doSimpleAction")
				.build());

		queue = defaultJobQueue().runOnceOnly().withPredefinedJobs(jobs).buildAndStart();
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		assertEquals(JobStatus.FINISHED, jobs.get(0).getStatus());
		assertEquals(valueToChangeTo, fieldToChange.get());
	}

	@Test
	public void ensureResourceIsLocked() throws Throwable {
		int originalValue = 10;
		int valueToChangeToFirst = 11;
		int valueToChangeToLater = 12;
		String resourceKey = "ensureResourceIsLocked";
		AtomicInteger fieldToChange = new AtomicInteger(originalValue);

		Job jobLockResource = Job.builder()
				.withAction((jobRef) -> {
					Thread.sleep(600);
					fieldToChange.set(valueToChangeToFirst);
				})
				.withResourceKey(resourceKey)
				.build();
		Job jobWaitForResource = Job.builder()
				.withAction((jobRef) -> {
					assertEquals(valueToChangeToFirst, fieldToChange.get(), "Expected value to be changed before this: " + valueToChangeToFirst);
					fieldToChange.set(valueToChangeToLater);
				})
				.withResourceKey(resourceKey)
				.build();

		queue = defaultJobQueue().buildAndStart();
		queue.add(jobLockResource);
		queue.add(jobWaitForResource);


		Thread.sleep(200);
		assertEquals(JobStatus.RUNNING_ACTION, jobLockResource.getStatus(), "Expected the locking job to have the the status " + JobStatus.RUNNING_ACTION);
		assertEquals(JobStatus.WAITING_FOR_RESOURCE, jobWaitForResource.getStatus(), "Expected the waiting job to have the the status " + JobStatus.WAITING_FOR_RESOURCE);
		assertEquals(originalValue, fieldToChange.get(), "Expected value to be unchanged from the original: " + originalValue);
		queue.stopAndAwait(2000, TimeUnit.SECONDS);
		assertEquals(valueToChangeToLater, fieldToChange.get(), "Expected value to be finished with: " + valueToChangeToLater);
		assertFalse(jobWaitForResource.hasThrowable(), "Expected no exceptions");

		assertEquals(0, jobQueueService.getPendingJobs().size());
		assertEquals(0, jobQueueService.getRunningJobs().size());
		assertEquals(2, jobQueueService.getFinishedJobs().size());
	}

	@Test
	public void canCatchExceptions() {
		final Job job = Job.builder()
				.withAction((jobRef) -> {
					throw new NullPointerException("test");
				})
				.onError((jobReference, throwable) -> LOGGER.info("Caught exception successfully for job " + jobReference.getJobId()))
				.build();

		queue = defaultJobQueue(false)
				.runOnceOnly()
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);
		assertTrue(job.hasThrowable());
		assertEquals(NullPointerException.class, job.getThrowable().getClass());
		assertEquals(job.getThrowable().getMessage(), "test");
	}

	@Test
	public void canAddExceptionHandlingAfterJobIsCreated() {
		AtomicInteger fieldToChange = new AtomicInteger(0);

		final Job job = Job.builder()
				.withAction((jobRef) -> {
					throw new IllegalStateException("test");
				})
				.onError(((jobRef, throwable) -> {
					LOGGER.info("Caught exception successfully for job " + jobRef.getJobId());
					assertEquals(1, fieldToChange.getAndIncrement(), "Expected the job's first defined on error to be executed");
				}))
				.onFinalize(jobRef -> {
					assertEquals(3, fieldToChange.getAndIncrement(), "Expected all error callbacks to be called before this onFinalize");
				})
				.build();

		job.appendOnJobError((jobRef, throwable) -> {
			assertEquals(2, fieldToChange.getAndIncrement(), "Expected the later added onJobError to be executed last");
		});

		queue = defaultJobQueue(false)
				.runOnceOnly()
				.withPredefinedJobs(Collections.singletonList(job))
				.withOnErrorHandler((jobRef, throwable) -> {
					assertEquals(0, fieldToChange.getAndIncrement(), "Expected queue global error handler to be executed first");
				})
				.buildAndStart();
		queue.stopAndAwait(1, TimeUnit.SECONDS);
		assertEquals(IllegalStateException.class, job.getThrowable().getClass());
		assertEquals(job.getThrowable().getMessage(), "test");
		assertEquals(4, fieldToChange.get());
	}

	@Test
	public void canSetCustomJobIds() {
		final long jobId = 99;
		final Job job = Job.builder()
				.withJobId(jobId)
				.withAction((jobRef) -> { })
				.build();

		queue = defaultJobQueue(false)
				.runOnceOnly()
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);

		Job fetchedJob = queue.getJobById(jobId);
		assertEquals(job, fetchedJob);
		assertEquals(job.getJobId(), jobId);
	}

	@Test
	public void canSetCustomJobIdPostCreationAndWontStartUnlessSet() {
		final Job job = Job.builder()
				.setJobIdAfterBuild()
				.build();
		assertEquals(JobStatus.WAITING_FOR_ID, job.getStatus());

		queue = defaultJobQueue(false)
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);

		List<Job> fetchedJobs = queue.getAllJobs();
		assertEquals(0, fetchedJobs.size());
	}

	@Test
	public void canSetCustomJobIdPostCreationAndIsStartingWhenSet() throws Throwable {
		final Job job = Job.builder()
				.setJobIdAfterBuild()
				.build();

		queue = defaultJobQueue(false)
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		Thread.sleep(100);

		job.setJobId(1);
		Thread.sleep(100);
		queue.stopAndAwait(1000, TimeUnit.SECONDS);

		List<Job> fetchedJobs = queue.getAllJobs();
		assertEquals(1, fetchedJobs.size());
		assertEquals(1, fetchedJobs.get(0).getJobId());
		assertEquals(JobStatus.FINISHED, fetchedJobs.get(0).getStatus());
	}

	@Test
	public void jobActionCanReferToItself() {
		final long jobId = 50;
		final Job job = Job.builder()
				.withJobId(jobId)
				.withAction((jobRef) -> { })
				.build();

		queue = defaultJobQueue(false)
				.runOnceOnly()
				.withPredefinedJobs(Collections.singletonList(job))
				.buildAndStart();
		queue.stopAndAwait(1000, TimeUnit.SECONDS);

		Job fetchedJob = queue.getJobById(jobId);
		assertEquals(job, fetchedJob);
		assertEquals(job.getJobId(), jobId);
	}

}
