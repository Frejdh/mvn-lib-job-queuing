package com.frejdh.util.job.tests;

import com.frejdh.util.common.toolbox.CommonUtils;
import com.frejdh.util.common.toolbox.ReflectionUtils;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.persistence.DaoService;
import com.frejdh.util.job.persistence.config.DaoPersistenceMode;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RunWith(Theories.class) // To run multiple DAO configurations with the same tests
public abstract class AbstractQueueTests {

	protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	protected JobQueue queue;

	@DataPoints
	public static final List<DaoPersistenceMode> PERSISTENCE_MODES = getPersistenceModes();

	private static List<DaoPersistenceMode> getPersistenceModes() {
		return Arrays.stream(DaoPersistenceMode.values())
				.filter(mode -> !mode.equals(DaoPersistenceMode.CUSTOM))
				.collect(Collectors.toList());
	}

	@SneakyThrows
	protected DaoService getDaoService() {
		Assert.assertNotNull("'queue' object cannot be null when fetching the DAO service", queue);
		return ReflectionUtils.getVariable(queue, "daoService", DaoService.class);
	}

	@After
	public void afterTests() {
		if (queue != null) {
			queue.stopNow();
		}
	}
}
