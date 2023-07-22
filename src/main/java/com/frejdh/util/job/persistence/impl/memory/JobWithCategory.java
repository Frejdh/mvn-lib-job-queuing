package com.frejdh.util.job.persistence.impl.memory;

import com.frejdh.util.job.Job;
import com.frejdh.util.job.model.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobWithCategory {

	private Job job;
	private JobCategory category;

	public JobWithCategory(Job job) {
		this.job = job;
		this.setCategory(toJobCategory(job));
	}

	public static JobCategory toJobCategory(@NotNull Job job) {
		JobStatus status = job.getStatus();
		if (status.isPending()) {
			return JobCategory.PENDING;
		}
		else if (status.isRunning()) {
			return JobCategory.RUNNING;
		}
		return JobCategory.FINISHED;
	}
}
