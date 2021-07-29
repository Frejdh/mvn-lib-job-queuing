package com.frejdh.util.job.persistence;

import com.frejdh.util.environment.Config;
import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.config.DaoPersistence;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;


/**
 * Default config DAO-service.
 * Get the configured DaoService accordingly to the environmental properties.
 */
public class DaoService extends AbstractDaoService {

	private static final DaoPersistence DEFAULT_CONFIGURATION = new DaoPersistence(DaoPersistenceMode.RUNTIME);
	private final AbstractDaoService impl;

	@SneakyThrows
	public DaoService() {
		this.impl = getImplementation();
	}

	@SneakyThrows
	private AbstractDaoService getImplementation() {
		DaoPersistence config = getImplementationConfigFromProperties();
		return config.getImplementationClass().getDeclaredConstructor().newInstance();
	}

	private DaoPersistence getImplementationConfigFromProperties() {
		DaoPersistenceMode mode = DaoPersistenceMode.toEnum(Config.getString(ARG_PERSISTENCE_MODE));
		if (mode == null) {
			return DEFAULT_CONFIGURATION;
		}
		else if (mode == DaoPersistenceMode.CUSTOM) {
			return new DaoPersistence(getDaoImplementationClassFromClasspath());
		}
		return new DaoPersistence(mode);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends AbstractDaoService> getDaoImplementationClassFromClasspath() {
		String classpath = Config.getString(ARG_IMPLEMENTATION_CLASS);
		Class<? extends AbstractDaoService> classToUse;
		try {
			classToUse = classpath != null
					? (Class<? extends AbstractDaoService>) Class.forName(classpath)
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
		System.out.println("Updating: " + job.getStatus());
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
	public Job getRunningJobById(Long jobId) {
		return impl.getRunningJobById(jobId);
	}

	@Override
	public Map<Long, Job> getRunningJobs() {
		return impl.getRunningJobs();
	}

	@Override
	public Map<String, Job> getRunningJobsForResources() {
		return impl.getRunningJobsForResources();
	}

	@Override
	public Job getRunningJobByResource(String resource) {
		return impl.getRunningJobByResource(resource);
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
}
