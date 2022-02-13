package com.frejdh.util.job.persistence.config;

import com.frejdh.util.job.persistence.AbstractJobQueueDao;
import com.frejdh.util.job.persistence.impl.h2.H2JobQueueDao;
import com.frejdh.util.job.persistence.impl.memory.RuntimeJobQueueDao;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public enum DaoPersistenceMode {
	H2(H2JobQueueDao.class, "h2"),
	RUNTIME(RuntimeJobQueueDao.class, "runtime", "in-memory", "memory", "internal"),
	CUSTOM(null, "custom");
	private final Class<? extends AbstractJobQueueDao> implementationClass;
	private final List<String> aliases;

	DaoPersistenceMode(Class<? extends AbstractJobQueueDao> implementation, String... aliases) {
		this.implementationClass = implementation;
		this.aliases = Arrays.asList(aliases);
	}

	public Class<? extends AbstractJobQueueDao> getImplementationClass() {
		return implementationClass;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public static DaoPersistenceMode toEnum(String alias) {
		if (alias == null) {
			return null;
		}

		return Arrays.stream(values())
				.filter(daoEnum -> daoEnum.aliases.contains(alias.toLowerCase(Locale.ROOT)))
				.findFirst().orElse(null);
	}

	public static Class<? extends AbstractJobQueueDao> toImplementationClass(String arg) {
		DaoPersistenceMode foundEnum = toEnum(arg);
		return foundEnum != null ? foundEnum.implementationClass : null;
	}

}
