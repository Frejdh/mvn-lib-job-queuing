package com.frejdh.util.job.persistence;

import com.frejdh.util.common.toolbox.ReflectionUtils;
import com.frejdh.util.job.Job;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractJobQueueDao {

	public AbstractJobQueueDao() { }


	protected AtomicLong lastJobId = new AtomicLong(0);

	abstract public Job updateJob(@NotNull Job job);

	abstract public Job updateJobOnlyOnFreeResource(@NotNull Job job);

	/**
	 * Adds a job to the persistence layer. Job ID must be set during this stage.
	 * @param job Job to add
	 * @return The added job or null
	 */
	abstract public Job addJob(@NotNull Job job);

	abstract public boolean addToPendingJobs(Job job);

	abstract public boolean addToCurrentJobs(Job job);

	abstract public boolean addToFinishedJobs(Job job);

	abstract protected Job removeJob(@NotNull Job job);

	abstract public Job getJobById(Long id);

	abstract public Job getPendingJobById(Long jobId);

	abstract public List<Job> getPendingJobsByResource(String resource);

	abstract public Job getCurrentJobById(Long jobId);

	abstract public Map<Long, Job> getCurrentJobs();

	abstract public Map<String, Job> getCurrentJobsForResources();

	abstract public Job getCurrentJobByResource(String resource);

	abstract public Map<Long, Job> getFinishedJobsById();

	abstract public List<Job> getFinishedJobsByResource(String resource);

	abstract public Job getLastAddedJob();

	abstract public Job getLastFinishedJob();

	abstract public Map<Long, Job> getPendingJobs();

	abstract public Map<Long, Job> getFinishedJobs();

	abstract public List<Job> getAllJobs();

	protected boolean isPendingJob(Job job) {
		return job != null && job.isStarted();
	}

	protected boolean isCurrentJob(Job job) {
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

	@SneakyThrows
	protected void setJobId(Job job, long id) {
		ReflectionUtils.invokeMethod(job, "internalSetJobId", id);
	}
}
