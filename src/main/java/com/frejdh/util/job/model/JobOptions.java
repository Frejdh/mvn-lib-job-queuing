package com.frejdh.util.job.model;

import lombok.Builder;
import lombok.Getter;
import java.util.concurrent.TimeUnit;

@Builder(toBuilder = true, setterPrefix = "with")
@Getter
public class JobOptions {

	private final long timeout;

	public static class JobOptionsBuilder {
		private long timeout = 0L;

		public JobOptionsBuilder setTimeout(long timeout, TimeUnit unit) {
			if (unit != null) {
				this.timeout = unit.toMillis(timeout);
			}
			return this;
		}

		public JobOptionsBuilder setTimeout(long timeoutMillis) {
			this.timeout = timeoutMillis;
			return this;
		}
	}

	@Override
	public String toString() {
		return "JobOptions{" +
				"timeout=" + timeout +
				'}';
	}
}
