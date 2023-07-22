package com.frejdh.util.job.persistence;

import com.frejdh.util.job.Job;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractJobQueueDao {

	public AbstractJobQueueDao() { }

	protected AtomicLong lastJobId = new AtomicLong(0);

	/**
	 * Adds/updates a job to the persistence layer. Job ID will be created at this stage if not flagged to be created later.
	 * @param job Job to add
	 * @return The added job or null
	 */
	abstract public Job upsertJob(@NotNull Job job);

	abstract protected Job removeJob(@NotNull Job job);

	abstract public Job getJobById(Long jobId);

	abstract public Job getLastAddedJob();

	abstract public Job getLastFinishedJob();

	abstract public Map<Long, Job> getPendingJobs();

	abstract public Map<Long, Job> getRunningJobs();

	abstract public Map<Long, Job> getFinishedJobs();

	abstract public List<Job> getAllJobs();

	protected boolean isPendingJob(Job job) {
		return job != null && job.isStarted();
	}

	protected boolean isRunningJob(Job job) {
		return job != null && job.isRunning();
	}

	protected boolean isFinishedJob(Job job) {
		return job != null && job.isFinished();
	}

	@SafeVarargs
	protected final <T> T getFirstOrNull(T... objects) {
		for (T obj : objects) {
			if (obj != null) {
				return obj;
			}
		}
		return null;
	}

	public boolean isResourceFree(String resource) {
		if (resource == null) {
			return true;
		}

		return getRunningJobs().values().stream()
				.noneMatch(job -> Optional.ofNullable(job.getResourceKey()).orElse("").equals(resource));
	}

}
