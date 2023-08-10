package com.frejdh.util.job.tests;

import com.frejdh.util.job.persistence.JobQueueService;
import com.frejdh.util.job.persistence.impl.memory.RuntimeJobQueueDao;


public class JobQueueRuntimeDaoTest extends AbstractJobQueueDaoTest {

	protected JobQueueRuntimeDaoTest() {
		super(new JobQueueService(new RuntimeJobQueueDao()));
	}

}
