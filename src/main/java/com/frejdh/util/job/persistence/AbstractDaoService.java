package com.frejdh.util.job.persistence;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import org.jetbrains.annotations.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractDaoService {

	public AbstractDaoService() {
		// Empty
	}

	protected long lastJobId;

	abstract public void addJobDependingOnStatus(@NotNull List<Job> jobs);

	abstract public void addToPendingJobs(Job job);

	abstract public boolean addToCurrentJobs(Job job);

	abstract protected void removeCurrentJob(Job job);

	abstract public Job getJobById(Long id);

	abstract public Map<Long, Job> getPendingJobsById();

	abstract public Job getPendingJobById(Long jobId);

	abstract public List<Job> getPendingJobsByResource(String resource);

	abstract public Job getRunningJobById(Long jobId);

	abstract public Map<Long, Job> getRunningJobsById();

	abstract public Map<String, Job> getRunningJobsByResource();

	abstract public Job getRunningJobByResource(String resource);

	abstract public Map<Long, Job> getFinishedJobsById();

	abstract public List<Job> getFinishedJobsByResource(String resource);

	abstract public Job getLastAddedJob();

	abstract public Job getLastFinishedJob();

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
