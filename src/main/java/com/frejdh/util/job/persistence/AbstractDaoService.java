package com.frejdh.util.job.persistence;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import org.jetbrains.annotations.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public abstract class AbstractDaoService {

	public static final String ARG_PERSISTENCE_MODE = "config.persistence.mode";
	public static final String ARG_IMPLEMENTATION_CLASS = "config.persistence.custom.implementation-class";

	public AbstractDaoService() {
		// Empty
	}

	protected AtomicLong lastJobId = new AtomicLong(0);

	abstract public Job addJob(@NotNull Job job);

	abstract public Job updateJob(@NotNull Job job);

	abstract public Job updateJobOnlyOnFreeResource(@NotNull Job job);

	abstract protected Job removeJob(@NotNull Job job);

	abstract public Job getJobById(Long id);

	abstract public Map<Long, Job> getPendingJobs();

	abstract public Job getPendingJobById(Long jobId);

	abstract public List<Job> getPendingJobsByResource(String resource);

	abstract public Job getRunningJobById(Long jobId);

	abstract public Map<Long, Job> getRunningJobs();

	abstract public Map<String, Job> getRunningJobsForResources();

	abstract public Job getRunningJobByResource(String resource);

	abstract public Map<Long, Job> getFinishedJobsById();

	abstract public List<Job> getFinishedJobsByResource(String resource);

	abstract public Job getLastAddedJob();

	abstract public Job getLastFinishedJob();

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
}
