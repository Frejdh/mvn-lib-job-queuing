package com.frejdh.util.job.persistence.impl.memory;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.AbstractDaoService;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class InMemoryDaoService extends AbstractDaoService {

	protected final Map<Long, Job> pendingJobs = new LinkedHashMap<>();
	protected final Map<Long, Job> currentJobsById = new LinkedHashMap<>();
	protected final Map<String, Job> currentJobsByResource = new LinkedHashMap<>();
	protected final Map<Long, Job> finishedJobs = new LinkedHashMap<>();
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	@Override
	public void addJobDependingOnStatus(@NotNull List<Job> jobs) {
		jobs.forEach(job -> {
			if (job.isFinished()) {
				finishedJobs.put(job.getJobId(), job);
			}
			else {
				pendingJobs.put(job.getJobId(), job);
			}
		});
	}

	@Override
	public void addToPendingJobs(Job job) {
		synchronized (pendingJobs) {
			this.pendingJobs.put(job.getJobId(), job.toBuilder()
					.withJobId(lastJobId++)
					.withStatus(JobStatus.ADDED_TO_QUEUE)
					.build()
			);
			// runScheduler(false);
		}
	}

	/**
	 * Attempt to add a job to the "current" job list/mapping, also removes from the "pending" jobs list/mapping.
	 *
	 * @param job Job to add
	 * @return True if the job was added, false if the resource was busy.
	 */
	@Override
	public boolean addToCurrentJobs(Job job) {
		synchronized (currentJobsByResource) {
			if (currentJobsByResource.putIfAbsent(job.getResourceKey(), job) != null) {
				return false;
			}
			this.currentJobsById.put(job.getJobId(), job);
			this.pendingJobs.remove(job.getJobId());
		}
		return true;
	}

	/**
	 * Helper method. Removes the current job.
	 *
	 * @param job Job to remove
	 */
	@Override
	public synchronized void removeCurrentJob(Job job) {
		this.currentJobsById.remove(job.getJobId());
		this.currentJobsByResource.remove(job.getResourceKey());
		this.currentJobFuturesByJobId.remove(job.getJobId());
		this.finishedJobs.put(job.getJobId(), job);
	}

	@Override
	public Job getJobById(Long id) {
		return pendingJobs.getOrDefault(id,
				currentJobsById.getOrDefault(id,
						finishedJobs.get(id)
				)
		);
	}

	@Override
	public Map<Long, Job> getPendingJobsById() {
		return new LinkedHashMap<>(pendingJobs);
	}

	@Override
	public Job getPendingJobById(Long jobId) {
		return pendingJobs.get(jobId);
	}

	@Override
	public List<Job> getPendingJobsByResource(String resource) {
		return pendingJobs.values()
				.stream()
				.filter(job -> job.getResourceKey().equals(resource))
				.collect(Collectors.toList());
	}

	@Override
	public Job getRunningJobById(Long jobId) {
		return currentJobsById.get(jobId);
	}

	@Override
	public Map<Long, Job> getRunningJobsById() {
		return new LinkedHashMap<>(currentJobsById);
	}

	@Override
	public Map<String, Job> getRunningJobsByResource() {
		return new LinkedHashMap<>(currentJobsByResource);
	}

	@Override
	public Job getRunningJobByResource(String resource) {
		return currentJobsByResource.get(resource);
	}

	@Override
	public Map<Long, Job> getFinishedJobsById() {
		return new LinkedHashMap<>(finishedJobs);
	}

	@Override
	public List<Job> getFinishedJobsByResource(String resource) {
		return finishedJobs.values()
				.stream()
				.filter(job -> job.getResourceKey().equals(resource))
				.collect(Collectors.toList());
	}

	@Override
	public Job getLastAddedJob() {
		synchronized (this) {
			return getFirstOrNull(
					getLastJobOfOrderedMap(pendingJobs),
					getLastJobOfOrderedMap(currentJobsById),
					getLastJobOfOrderedMap(finishedJobs)
			);
		}
	}

	@Override
	public Job getLastFinishedJob() {
		synchronized (this) {
			return getLastJobOfOrderedMap(finishedJobs);
		}
	}

	private Job getLastJobOfOrderedMap(Map<Long, Job> jobMap) {
		if (jobMap.isEmpty()) {
			return null;
		}

		List<Map.Entry<Long, Job>> entryList = new ArrayList<>(jobMap.entrySet());
		return entryList.get(entryList.size() - 1).getValue();
	}


}
