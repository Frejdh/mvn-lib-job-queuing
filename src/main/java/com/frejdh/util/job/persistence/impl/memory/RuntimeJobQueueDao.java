package com.frejdh.util.job.persistence.impl.memory;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuntimeJobQueueDao extends AbstractJobQueueDao {

	protected final Map<Long, Job> pendingJobs = new LinkedHashMap<>();
	protected final Map<Long, Job> currentJobsById = new LinkedHashMap<>();
	protected final Map<String, Job> currentJobsByResource = new LinkedHashMap<>();
	protected final Map<Long, Job> finishedJobs = new LinkedHashMap<>();

	@Override
	public Job addJob(@NotNull Job job) {
		if (job.getStatus().isPending()) {
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

	public boolean addToPendingJobs(Job job) {
		if (job.getStatus().isWaitingForId()) {
			return false;
		}

		synchronized (pendingJobs) {
			if (!job.hasJobId()) {
				setJobId(job, lastJobId.getAndIncrement());
			}
			return this.pendingJobs.put(job.getJobId(), job) != null;
		}
	}

	/**
	 * Attempt to add a job to the "current" job list/mapping, also removes from the "pending" jobs list/mapping.
	 *
	 * @param job Job to add
	 * @return True if the job was added, false if the resource was busy.
	 */
	public boolean addToCurrentJobs(Job job) {
		synchronized (currentJobsByResource) {
			if (job.hasStartedAlready() || (job.getResourceKey() != null && currentJobsByResource.putIfAbsent(job.getResourceKey(), job) != null)) {
				return false;
			}
			this.pendingJobs.remove(job.getJobId());
			this.finishedJobs.remove(job.getJobId());
			this.currentJobsById.put(job.getJobId(), job);
		}
		return true;
	}

	public boolean addToFinishedJobs(Job job) {
		synchronized (finishedJobs) {
			this.currentJobsById.remove(job.getJobId());
			this.currentJobsByResource.remove(job.getResourceKey());
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
	public Map<Long, Job> getFinishedJobs() {
		return finishedJobs;
	}

	@Override
	public List<Job> getAllJobs() {
		return Stream.of(pendingJobs.values(), currentJobsById.values(), finishedJobs.values())
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
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
	public Job getCurrentJobById(Long jobId) {
		return currentJobsById.get(jobId);
	}

	@Override
	public Map<Long, Job> getCurrentJobs() {
		return new LinkedHashMap<>(currentJobsById);
	}

	@Override
	public Map<String, Job> getCurrentJobsForResources() {
		return new LinkedHashMap<>(currentJobsByResource);
	}

	@Override
	public Job getCurrentJobByResource(String resource) {
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
