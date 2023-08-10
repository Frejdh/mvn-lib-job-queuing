package com.frejdh.util.job.tests;

import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.persistence.JobQueueService;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import com.frejdh.util.job.persistence.impl.memory.RuntimeJobQueueDao;
import lombok.SneakyThrows;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RunWith(Theories.class) // To run multiple DAO configurations with the same tests
public abstract class AbstractQueueTests {

	protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	private static JobQueueService JOB_QUEUE_SERVICE;
	protected JobQueue queue;

	@DataPoints
	public static final List<DaoPersistenceMode> PERSISTENCE_MODES = getPersistenceModes();

	private static List<DaoPersistenceMode> getPersistenceModes() {
		return Arrays.stream(DaoPersistenceMode.values())
				.filter(mode -> !mode.equals(DaoPersistenceMode.CUSTOM))
				.collect(Collectors.toList());
	}

	@SneakyThrows
	protected static JobQueueService getJobQueueService() {
		return JOB_QUEUE_SERVICE;
	}

	@BeforeEach
	public void beforeTests() {
		JOB_QUEUE_SERVICE = new JobQueueService(new RuntimeJobQueueDao());
		queue = null;
	}

	@AfterEach
	public void afterTests() {
		if (queue != null) {
			queue.stopNow();
		}
	}
}
