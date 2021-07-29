package com.frejdh.util.job.persistence.impl.runtime;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.AbstractDaoService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RuntimeDaoService extends AbstractDaoService {

	protected final Map<Long, Job> pendingJobs = new LinkedHashMap<>();
	protected final Map<Long, Job> currentJobsById = new LinkedHashMap<>();
	protected final Map<String, Job> currentJobsByResource = new LinkedHashMap<>();
	protected final Map<Long, Job> finishedJobs = new LinkedHashMap<>();

	@Override
	public Job addJob(@NotNull Job job) {
		if (job.getStatus() == JobStatus.FAILED) {
			System.out.println("");
		}
		if (job.getStatus().isPending()) {
			if (job.getStatus().equals(JobStatus.INITIALIZED) && !job.hasJobId()) {
				long jobId;
				synchronized (this) {
					jobId = lastJobId.getAndIncrement();
				}
				job = job.toBuilder()
						.withJobId(jobId)
						.build();

			}
			return addToPendingJobs(job) ? job : null;
		}
		else if (job.getStatus().isRunning()) {
			return addToCurrentJobs(job) ? job : null;
		}
		return addToFinishedJobs(job) ? job : null;
	}

	@Override
	public Job updateJob(@NotNull Job job) {
		final JobStatus jobStatus = job.getStatus();
		final long jobId = job.getJobId();
		final boolean pendingJobsContainsKey = pendingJobs.containsKey(jobId);
		final boolean currentJobsContainsKey = currentJobsById.containsKey(jobId);
		final boolean finishedJobsContainsKey = finishedJobs.containsKey(jobId);

		if (!jobStatus.isPending() && pendingJobsContainsKey) {
			removePendingJob(job);
		}
		if (!jobStatus.isRunning() && currentJobsContainsKey) {
			removeCurrentJob(job);
		}
		if (!jobStatus.isDone() && finishedJobsContainsKey) {
			removeFinishedJob(job);
		}

		return addJob(job);
	}

	@Override
	public Job updateJobOnlyOnFreeResource(@NotNull Job job) {
		String resourceKey = job.getResourceKey();
		if (resourceKey == null || !currentJobsByResource.containsKey(resourceKey)) {
			return updateJob(job);
		}
		return null;
	}

	protected boolean addToPendingJobs(Job job) {
		synchronized (pendingJobs) {
			return this.pendingJobs.put(job.getJobId(), job) != null;
		}
	}

	/**
	 * Attempt to add a job to the "current" job list/mapping, also removes from the "pending" jobs list/mapping.
	 *
	 * @param job Job to add
	 * @return True if the job was added, false if the resource was busy.
	 */
	protected boolean addToCurrentJobs(Job job) {
		synchronized (currentJobsByResource) {
			if (job.getResourceKey() != null && currentJobsByResource.putIfAbsent(job.getResourceKey(), job) != null) {
				return false;
			}
			this.currentJobsById.put(job.getJobId(), job);
			this.pendingJobs.remove(job.getJobId());
		}
		return true;
	}

	protected boolean addToFinishedJobs(Job job) {
		synchronized (finishedJobs) {
			return this.finishedJobs.put(job.getJobId(), job) != null;
		}
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
	public Map<Long, Job> getPendingJobs() {
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
				.filter(job -> resource.equals(job.getResourceKey()))
				.collect(Collectors.toList());
	}

	@Override
	public Job getRunningJobById(Long jobId) {
		return currentJobsById.get(jobId);
	}

	@Override
	public Map<Long, Job> getRunningJobs() {
		return new LinkedHashMap<>(currentJobsById);
	}

	@Override
	public Map<String, Job> getRunningJobsForResources() {
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
				.filter(job -> resource.equals(job.getResourceKey()))
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
	protected Job removeJob(@NotNull Job job) {
		Job retval = removePendingJob(job);
		if (retval == null) {
			retval = removeFinishedJob(job);
		}
		if (retval == null) {
			retval = removeCurrentJob(job);
		}
		return retval;
	}

	private Job removePendingJob(Job job) {
		Job retval;
		synchronized (pendingJobs) {
			retval = pendingJobs.remove(job.getJobId());
		}

		return retval;
	}

	private Job removeCurrentJob(Job job) {
		Job retval;
		synchronized (currentJobsById) {
			retval = currentJobsById.remove(job.getJobId());
			if (job.getResourceKey() != null) {
				currentJobsByResource.remove(job.getResourceKey());
			}
		}

		return retval;
	}

	private Job removeFinishedJob(Job job) {
		Job retval;
		synchronized (finishedJobs) {
			retval = finishedJobs.remove(job.getJobId());
		}

		return retval;
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
