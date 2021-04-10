package com.frejdh.util.job;

import com.frejdh.util.job.model.JobOptions;
import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobQueue {

	protected ThreadPoolExecutor pool;
	protected JobOptions options;
	protected final Map<Long, Job> pendingJobs;
	protected final Map<Long, Job> currentJobsById;
	protected final Map<String, Job> currentJobsByResource;
	protected final Map<Long, Job> finishedJobs;
	private long lastJobId;

	JobQueue() {
		this.pendingJobs = new LinkedHashMap<>();
		this.currentJobsById = new LinkedHashMap<>();
		this.currentJobsByResource = new LinkedHashMap<>();
		this.finishedJobs = new LinkedHashMap<>();
	}

	JobQueue(List<Job> predefinedJobs) {
		this();
		addJobDependingOnStatus(predefinedJobs);
		this.lastJobId = finishedJobs.values().stream().mapToLong(Job::getJobId).max().orElse(0);
	}

	JobQueue(JobOptions options, @Nullable List<Job> persistedJobs) {
		this(persistedJobs);
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(options.getAmountOfThreads());
		this.options = options;
	}

	public static JobQueueBuilder getBuilder() {
		return new JobQueueBuilder();
	}

	public void start() {
		runScheduler(true);
		if (options.isRunningOnce()) {
			pool.shutdown();
		}
	}

	public void stop() {
		pool.shutdown();
	}

	public boolean stopAndAwait(long timeout, TimeUnit timeUnit) {
		pool.shutdown();
		boolean completedExecutions = false;
		try {
			completedExecutions = pool.awaitTermination(timeout, timeUnit);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return completedExecutions;
	}

	private void addJobDependingOnStatus(List<Job> jobs) {
		jobs.forEach(job -> {
			if (job.isFinished()) {
				finishedJobs.put(job.getJobId(), job);
			}
			else {
				pendingJobs.put(job.getJobId(), job);
			}
		});
	}

	public void add(Job job) {
		synchronized (pendingJobs) {
			job.setJobId(lastJobId++);
			this.pendingJobs.put(job.getJobId(), job);
			runScheduler(true);
		}
	}

	protected boolean addCurrentJob(Job job) {
		synchronized (currentJobsByResource) {
			if (currentJobsByResource.putIfAbsent(job.getResourceKey(), job) != null) {
				return false;
			}
			this.currentJobsById.put(job.getJobId(), job);
			this.pendingJobs.remove(job.getJobId());
		}
		return true;
	}

	void runScheduler(boolean addToPool) {
		for (Job job : pendingJobs.values()) {
			if (addCurrentJob(job)) {
				if (addToPool) {
					pool.submit(() -> executeJob(job));
				}
				else {
					executeJob(job);
				}
			}
		}
	}

	private void executeJob(Job job) {
		job.start();
		removeCurrentJob(job);
	}

	protected synchronized void removeCurrentJob(Job job) {
		this.currentJobsById.remove(job.getJobId());
		this.currentJobsByResource.remove(job.getResourceKey());
		this.finishedJobs.put(job.getJobId(), job);
		runScheduler(false);
	}

	public Job getJobById(Long id) {
		return pendingJobs.getOrDefault(id,
				currentJobsById.getOrDefault(id,
						finishedJobs.get(id)
				)
		);
	}

}
