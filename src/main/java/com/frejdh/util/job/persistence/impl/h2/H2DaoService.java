package com.frejdh.util.job.persistence.impl.h2;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractDaoServiceImpl;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class H2DaoService extends AbstractDaoServiceImpl {

	@Override
	public void addJobDependingOnStatus(@NotNull List<Job> jobs) {

	}

	@Override
	public void addToPendingJobs(Job job) {

	}

	@Override
	public boolean addToCurrentJobs(Job job) {
		return false;
	}

	@Override
	protected void addToFinishedJobs(Job job) {

	}

	@Override
	public Job getJobById(Long id) {
		return null;
	}

	@Override
	public Map<Long, Job> getPendingJobsById() {
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
	public Map<Long, Job> getRunningJobsById() {
		return null;
	}

	@Override
	public Map<String, Job> getRunningJobsByResource() {
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
}
