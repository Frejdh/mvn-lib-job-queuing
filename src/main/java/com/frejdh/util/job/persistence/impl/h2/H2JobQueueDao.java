package com.frejdh.util.job.persistence.impl.h2;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class H2JobQueueDao extends AbstractJobQueueDao {

	@Override
	public Job upsertJob(@NotNull Job job) {
		return null;
	}

	@Override
	protected Job removeJob(@NotNull Job job) {
		return null;
	}

	@Override
	public Job getJobById(Long jobId) {
		return null;
	}

	@Override
	public Job getLastAddedJob() {
		return null;
	}

	@Override
	public Job getLastFinishedJob() {
		return null;
	}

	@Override
	public Map<Long, Job> getPendingJobs() {
		return null;
	}

	@Override
	public Map<Long, Job> getRunningJobs() {
		return null;
	}

	@Override
	public Map<Long, Job> getFinishedJobs() {
		return null;
	}

	@Override
	public List<Job> getAllJobs() {
		return null;
	}
}
