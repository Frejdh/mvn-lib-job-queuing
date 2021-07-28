package com.frejdh.util.job.tests;

import com.frejdh.util.common.toolbox.CommonUtils;
import com.frejdh.util.common.toolbox.ReflectionUtils;
import com.frejdh.util.job.JobQueue;
import com.frejdh.util.job.persistence.DaoService;
import lombok.SneakyThrows;
import org.junit.After;
import java.util.logging.Logger;

public class AbstractQueueTests {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	protected JobQueue queue;

	@SneakyThrows
	protected DaoService getDaoService() {
		return ReflectionUtils.getVariable(queue, "daoService", DaoService.class);
	}

	@After
	public void afterTests() {
		if (queue != null) {
			queue.stopNow();
		}
	}
}
