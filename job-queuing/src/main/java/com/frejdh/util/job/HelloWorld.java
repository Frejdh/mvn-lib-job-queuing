package com.frejdh.util.job;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class HelloWorld {
	public static void main(String[] args) {
		JobFunction job = new JobFunction(() -> {
			System.out.println("action");
		}, (result) -> {
			System.out.println("callback");
		}, (error) -> {
			System.out.println(error);
		}, () -> {
			System.out.println("finalize");
		});

		JobQueue queue = JobQueue.getBuilder().setPredefinedJobs(
				Arrays.asList(new Job(job, "test"), new Job(job, "test"))
		).build();

		queue.start();

	}
}
