package com.frejdh.util.job.persistence.impl.memory;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.frejdh.util.job.persistence.impl.memory.JobWithCategory.toJobCategory;


public class RuntimeJobQueueDao extends AbstractJobQueueDao {

	protected final Map<Long, JobWithCategory> jobs = new LinkedHashMap<>();

	@Override
	public Job upsertJob(@NotNull Job job) {
		if (job.getStatus().isWaitingForId()) {
			return null;
		}
		else if (!job.hasJobId()) {
			job.setJobId(lastJobId.getAndIncrement());
		}

		if (!jobs.containsKey(job.getJobId())) {
			jobs.put(job.getJobId(), new JobWithCategory(job));
		}
		else {
			jobs.get(job.getJobId()).setCategory(toJobCategory(job));
		}
		return job;
	}

	@Override
	public Job getJobById(Long id) {
		return Optional.ofNullable(jobs.get(id)).map(JobWithCategory::getJob).orElse(null);
	}

	@SafeVarargs
	private final Map<Long, Job> filterJobs(Predicate<Job>... filters) {
		Predicate<Job> composedFilter = (job -> true);
		for (Predicate<Job> filter : filters) {
			composedFilter = composedFilter.and(filter);
		}

		Predicate<Job> finalComposedFilter = composedFilter;
		return jobs.entrySet().stream()
				.filter(entry -> finalComposedFilter.test(entry.getValue().getJob()))
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getJob()));
	}

	@Override
	public Map<Long, Job> getPendingJobs() {
		return filterJobs(job -> job.getStatus().isPending());
	}

	@Override
	public Map<Long, Job> getFinishedJobs() {
		return filterJobs(job -> job.getStatus().isFinished());
	}

	@Override
	public List<Job> getAllJobs() {
		return jobs.values().stream().map(JobWithCategory::getJob).collect(Collectors.toList());
	}

	@Override
	public Map<Long, Job> getRunningJobs() {
		return filterJobs(job -> job.getStatus().isRunning());
	}

	@Override
	public Job getLastAddedJob() {
		return getLastJobOfOrderedMap(filterJobs());
	}

	@Override
	protected Job removeJob(@NotNull Job job) {
		JobWithCategory jobWithCategory = jobs.remove(job.getJobId());
		return jobWithCategory != null ? jobWithCategory.getJob() : null;
	}

	@Override
	public Job getLastFinishedJob() {
		synchronized (this) {
			return getLastJobOfOrderedMap(getFinishedJobs());
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
