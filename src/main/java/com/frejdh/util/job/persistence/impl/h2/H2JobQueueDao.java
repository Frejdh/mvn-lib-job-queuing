package com.frejdh.util.job.persistence.impl.h2;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class H2JobQueueDao extends AbstractJobQueueDao {

	@Override
	public boolean addToPendingJobs(Job job) {
		return false;
	}

	@Override
	public boolean addToCurrentJobs(Job job) {
		return false;
	}

	@Override
	public boolean addToFinishedJobs(Job job) {
		return false;
	}

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
	public Job getPendingJobById(Long jobId) {
		return null;
	}

	@Override
	public List<Job> getPendingJobsByResource(String resource) {
		return null;
	}

	@Override
	public Job getCurrentJobById(Long jobId) {
		return null;
	}


	@Override
	public Map<String, Job> getCurrentJobsForResources() {
		return null;
	}

	@Override
	public Job getCurrentJobByResource(String resource) {
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

	@Override
	public Map<Long, Job> getPendingJobs() {
		return null;
	}

	@Override
	public Map<Long, Job> getCurrentJobs() {
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
