package com.frejdh.util.job.persistence;

import com.frejdh.util.environment.Config;
import com.frejdh.util.job.Job;
import com.frejdh.util.job.persistence.impl.memory.RuntimeDaoService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

public class DaoService extends AbstractDaoServiceImpl {

	private final AbstractDaoServiceImpl impl;
	private static final String CLASSPATH_IMPL_ARG = "config.persistence.implementation-class";
	private static final Class<? extends AbstractDaoServiceImpl> DEFAULT_IMPL_CLASS = RuntimeDaoService.class;
	protected final Map<Long, Future<?>> currentJobFuturesByJobId = new HashMap<>();

	@SneakyThrows
	public DaoService() {
		this.impl = mapClasspathArgumentToClass();
	}

	@SuppressWarnings("unchecked")
	private AbstractDaoServiceImpl mapClasspathArgumentToClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String classpathArg = Config.getString(CLASSPATH_IMPL_ARG, DEFAULT_IMPL_CLASS.getCanonicalName());
		if (StringUtils.isBlank(classpathArg)) {
			throw new ClassNotFoundException("Missing value for implementation class for argument '" + CLASSPATH_IMPL_ARG + "'");
		}

		return (Objects.requireNonNull(!classpathArg.contains(".")
				? PredefinedDaoImplementation.toImplementationClass(classpathArg)
				: (Class<? extends AbstractDaoServiceImpl>) Class.forName(classpathArg))
		).newInstance();
	}

	@Override
	public void addJobDependingOnStatus(@NotNull List<Job> jobs) {
		this.impl.addJobDependingOnStatus(jobs);
	}

	@Override
	public void addToPendingJobs(Job job) {
		this.impl.addToPendingJobs(job);
	}

	@Override
	public boolean addToCurrentJobs(Job job) {
		return this.impl.addToCurrentJobs(job);
	}

	@Override
	public void addToFinishedJobs(Job job) {
		this.impl.addToFinishedJobs(job);
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

	@Override
	public Map<Long, Job> getPendingJobs() {
		return this.impl.getPendingJobs();
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
