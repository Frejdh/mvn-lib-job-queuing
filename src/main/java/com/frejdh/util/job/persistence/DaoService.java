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
public class DaoService extends AbstractDaoServiceImpl {

	private final AbstractDaoServiceImpl impl;
	private static final DaoPersistence DEFAULT_CONFIGURATION = new DaoPersistence(DaoPersistenceMode.RUNTIME);
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	@SneakyThrows
	public DaoService() {
		this.impl = getImplementation();
	}

	@SneakyThrows
	private AbstractDaoServiceImpl getImplementation() {
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
	private Class<? extends AbstractDaoServiceImpl> getDaoImplementationClassFromClasspath() {
		String classpath = Config.getString(JobQueueConfigParameters.ARG_IMPLEMENTATION_CLASS);
		Class<? extends AbstractDaoServiceImpl> classToUse;
		try {
			classToUse = classpath != null
					? (Class<? extends AbstractDaoServiceImpl>) Class.forName(classpath)
					: DEFAULT_CONFIGURATION.getImplementationClass();
		} catch (ClassNotFoundException e) {
			classToUse = DEFAULT_CONFIGURATION.getImplementationClass();
		}
		return classToUse;
	}

	@Override
	public Job addJob(@NotNull Job job) {
		return impl.addJob(job);
	}

	@Override
	public Job updateJob(@NotNull Job job) {
		return impl.updateJob(job);
	}

	@Override
	public Job updateJobOnlyOnFreeResource(@NotNull Job job) {
		return impl.updateJobOnlyOnFreeResource(job);
	}

	@Override
	protected Job removeJob(@NotNull Job job) {
		return impl.removeJob(job);
	}

	@Override
	public boolean addToPendingJobs(Job job) {
		return this.impl.addToPendingJobs(job);
	}

	@Override
	public boolean addToCurrentJobs(Job job) {
		return this.impl.addToCurrentJobs(job);
	}

	@Override
	public boolean addToFinishedJobs(Job job) {
		return this.impl.addToFinishedJobs(job);
	}

	@Override
	public Job getJobById(Long id) {
		return impl.getJobById(id);
	}

	/**
	 * Get pending jobs in order. Oldest job starts at index 0.
	 * @return A map of the jobs, when converted to a collection it will be ordered.
	 */
	@Override
	public Map<Long, Job> getPendingJobs() {
		return impl.getPendingJobs();
	}

	@Override
	public Job getPendingJobById(Long jobId) {
		return impl.getPendingJobById(jobId);
	}

	@Override
	public List<Job> getPendingJobsByResource(String resource) {
		return impl.getPendingJobsByResource(resource);
	}

	@Override
	public Job getCurrentJobById(Long jobId) {
		return impl.getCurrentJobById(jobId);
	}


	@Override
	public Map<String, Job> getCurrentJobsForResources() {
		return impl.getCurrentJobsForResources();
	}

	@Override
	public Job getCurrentJobByResource(String resource) {
		return impl.getCurrentJobByResource(resource);
	}

	@Override
	public Map<Long, Job> getFinishedJobsById() {
		return impl.getFinishedJobsById();
	}

	@Override
	public List<Job> getFinishedJobsByResource(String resource) {
		return impl.getFinishedJobsByResource(resource);
	}

	@Override
	public Job getLastAddedJob() {
		return impl.getLastAddedJob();
	}

	@Override
	public Job getLastFinishedJob() {
		return impl.getLastFinishedJob();
	}



	@Override
	public Map<Long, Job> getCurrentJobs() {
		return this.impl.getCurrentJobs();
	}

	@Override
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

}
