package com.frejdh.util.job.persistence.impl.h2;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractDaoService;
import java.util.List;
import java.util.Map;

public class H2DaoService extends AbstractDaoService {

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
}
