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
	protected final Map<Long, Future<?>> runningJobFuturesByJobId = new HashMap<>();

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

	/**
	 * Add job to pending state. Adds jobId if missing and when the job doesn't have a "WAITING_FOR_ID" status.
	 * @param job to add.
	 * @return Whether it was added or not.
	 */
	public Job upsertJob(@NotNull Job job) {
		return impl.upsertJob(job);
	}

	public boolean isResourceFreeForJob(String resource) {
		return impl.isResourceFree(resource);
	}

	protected Job removeJob(@NotNull Job job) {
		return impl.removeJob(job);
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

	public Job getLastAddedJob() {
		return impl.getLastAddedJob();
	}

	public Job getLastFinishedJob() {
		return impl.getLastFinishedJob();
	}


	public Map<Long, Job> getRunningJobs() {
		return this.impl.getRunningJobs();
	}

	public Map<Long, Job> getFinishedJobs() {
		return this.impl.getFinishedJobs();
	}

	public Future<?> getRunningJobFutureByJobId(Long jobId) {
		return runningJobFuturesByJobId.get(jobId);
	}

	public Future<?> setRunningJobFutureByJobId(Long jobId, Future<?> future) {
		return runningJobFuturesByJobId.put(jobId, future);
	}

	public Future<?> removeRunningJobFutureByJobId(Long jobId) {
		return runningJobFuturesByJobId.remove(jobId);
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
