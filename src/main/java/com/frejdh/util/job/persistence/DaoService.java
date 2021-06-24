package com.frejdh.util.job.persistence;

import com.frejdh.util.environment.Config;
import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.impl.h2.H2DaoService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DaoService extends AbstractDaoService {

	private final AbstractDaoService impl;
	private static final String CLASSPATH_IMPL_ARG = "config.persistence.implementation-class";

	@SneakyThrows
	public DaoService() {
		this.impl = mapClasspathArgumentToClass();
	}

	@Override
	public boolean addToCurrentJobs(Job job) {
		return false;
	}

	@Override
	protected void removeCurrentJob(Job job) {

	}

	@SuppressWarnings("unchecked")
	private AbstractDaoService mapClasspathArgumentToClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String classpathArg = Config.getString(CLASSPATH_IMPL_ARG, H2DaoService.class.getCanonicalName());
		if (StringUtils.isBlank(classpathArg)) {
			throw new ClassNotFoundException("Missing value for implementation class for argument '" + CLASSPATH_IMPL_ARG + "'");
		}

		return (Objects.requireNonNull(!classpathArg.contains(".")
				? PredefinedDaoImplementation.toImplementationClass(classpathArg)
				: (Class<? extends AbstractDaoService>) Class.forName(classpathArg))
		).newInstance();
	}

	@Override
	public Job getJobById(Long id) {
		return impl.getJobById(id);
	}

	@Override
	public Map<Long, Job> getPendingJobsById() {
		return impl.getPendingJobsById();
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
	public Map<Long, Job> getRunningJobsById() {
		return impl.getRunningJobsById();
	}

	@Override
	public Map<String, Job> getRunningJobsByResource() {
		return impl.getRunningJobsByResource();
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
