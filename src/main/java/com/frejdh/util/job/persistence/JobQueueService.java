package com.frejdh.util.job.persistence;

import com.frejdh.util.environment.Config;
import com.frejdh.util.job.Job;
import com.frejdh.util.job.environment.JobQueueConfigParameters;
import com.frejdh.util.job.persistence.config.DaoPersistence;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Default config DAO-service.
 * Get the configured DaoService accordingly to the environmental properties.
 */
public class JobQueueService {

	private final AbstractJobQueueDao impl;
	private static final DaoPersistence DEFAULT_CONFIGURATION = new DaoPersistence(DaoPersistenceMode.RUNTIME);
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	@SneakyThrows
	public JobQueueService() {
		this.impl = getDefaultImplementation();
	}

	public JobQueueService(AbstractJobQueueDao implementation) {
		this.impl = implementation;
	}

	@SneakyThrows
	private AbstractJobQueueDao getDefaultImplementation() {
		DaoPersistence config = getImplementationConfigFromProperties();
		return config.getImplementationClass().getDeclaredConstructor().newInstance();
	}

	private DaoPersistence getImplementationConfigFromProperties() {
		DaoPersistenceMode mode = DaoPersistenceMode.toEnum(Config.getString(JobQueueConfigParameters.ARG_PERSISTENCE_MODE));
		if (mode == null) {
			return DEFAULT_CONFIGURATION;
		}
		else if (mode == DaoPersistenceMode.CUSTOM) {
			return new DaoPersistence(getDaoImplementationClassFromClasspath());
		}
		return new DaoPersistence(mode);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends AbstractJobQueueDao> getDaoImplementationClassFromClasspath() {
		String classpath = Config.getString(JobQueueConfigParameters.ARG_IMPLEMENTATION_CLASS);
		Class<? extends AbstractJobQueueDao> classToUse;
		try {
			classToUse = classpath != null
					? (Class<? extends AbstractJobQueueDao>) Class.forName(classpath)
					: DEFAULT_CONFIGURATION.getImplementationClass();
		} catch (ClassNotFoundException e) {
			classToUse = DEFAULT_CONFIGURATION.getImplementationClass();
		}
		return classToUse;
	}

	public Job addJob(@NotNull Job job) {
		return impl.addJob(job);
	}

	public Job updateJob(@NotNull Job job) {
		return impl.updateJob(job);
	}

	public Job updateJobOnlyOnFreeResource(@NotNull Job job) {
		return impl.updateJobOnlyOnFreeResource(job);
	}

	protected Job removeJob(@NotNull Job job) {
		return impl.removeJob(job);
	}

	/**
	 * Add job to pending state. Adds jobId if missing and when the job doesn't have a "WAITING_FOR_ID" status.
	 * @param job to add.
	 * @return Whether it was added or not.
	 */
	public boolean addToPendingJobs(Job job) {
		return this.impl.addToPendingJobs(job);
	}

	public boolean addToCurrentJobs(Job job) {
		return this.impl.addToCurrentJobs(job);
	}

	public boolean addToFinishedJobs(Job job) {
		return this.impl.addToFinishedJobs(job);
	}

	public Job getJobById(Long id) {
		return impl.getJobById(id);
	}

	/**
	 * Get pending jobs in order. Oldest job starts at index 0. Note, this doesn't mean that the job can be started yet.
	 * @return A map of the jobs, when converted to a collection it will be ordered.
	 */
	public Map<Long, Job> getPendingJobs() {
		return impl.getPendingJobs();
	}

	public Job getPendingJobById(Long jobId) {
		return impl.getPendingJobById(jobId);
	}

	public List<Job> getPendingJobsByResource(String resource) {
		return impl.getPendingJobsByResource(resource);
	}

	public Job getCurrentJobById(Long jobId) {
		return impl.getCurrentJobById(jobId);
	}


	public Map<String, Job> getCurrentJobsForResources() {
		return impl.getCurrentJobsForResources();
	}

	public Job getCurrentJobByResource(String resource) {
		return impl.getCurrentJobByResource(resource);
	}

	public Map<Long, Job> getFinishedJobsById() {
		return impl.getFinishedJobsById();
	}

	public List<Job> getFinishedJobsByResource(String resource) {
		return impl.getFinishedJobsByResource(resource);
	}

	public Job getLastAddedJob() {
		return impl.getLastAddedJob();
	}

	public Job getLastFinishedJob() {
		return impl.getLastFinishedJob();
	}


	public Map<Long, Job> getCurrentJobs() {
		return this.impl.getCurrentJobs();
	}

	public Map<Long, Job> getFinishedJobs() {
		return this.impl.getFinishedJobs();
	}

	public Future<?> getCurrentJobFuturesByJobId(Long jobId) {
		return currentJobFuturesByJobId.get(jobId);
	}

	public Future<?> setCurrentJobFuturesByJobId(Long jobId, Future<?> future) {
		return currentJobFuturesByJobId.put(jobId, future);
	}

	public Future<?> removeCurrentJobFuturesByJobId(Long jobId) {
		return currentJobFuturesByJobId.remove(jobId);
	}

	public boolean removeJobByJobId(long jobId) {
		Job job = impl.getJobById(jobId);
		if (job != null) {
			return impl.removeJob(job) != null;
		}
		return false;
	}

	public List<Job> getAllJobs() {
		return impl.getAllJobs();
	}

}
