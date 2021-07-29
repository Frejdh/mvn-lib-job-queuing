package com.frejdh.util.job.persistence.impl.h2;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractDaoService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class H2DaoService extends AbstractDaoService {

	@Override
	public Job addJob(@NotNull Job job) {
		return null;
	}

	@Override
	public Job updateJob(@NotNull Job job) {
		return null;
	}

	@Override
	public Job updateJobOnlyOnFreeResource(@NotNull Job job) {
		return null;
	}

	@Override
	protected Job removeJob(@NotNull Job job) {
		return null;
	}

	@Override
	public Job getJobById(Long id) {
		return null;
	}

	@Override
	public Map<Long, Job> getPendingJobs() {
		return null;
	}

	@Override
	public Job getPendingJobById(Long jobId) {
		return null;
	}

	@Override
	public List<Job> getPendingJobsByResource(String resource) {
		return null;
	}

	@Override
	public Job getRunningJobById(Long jobId) {
		return null;
	}

	@Override
	public Map<Long, Job> getRunningJobs() {
		return null;
	}

	@Override
	public Map<String, Job> getRunningJobsForResources() {
		return null;
	}

	@Override
	public Job getRunningJobByResource(String resource) {
		return null;
	}

	@Override
	public Map<Long, Job> getFinishedJobsById() {
		return null;
	}

	@Override
	public List<Job> getFinishedJobsByResource(String resource) {
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
}
