package com.frejdh.util.job.persistence.config;

import com.frejdh.util.job.persistence.AbstractDaoServiceImpl;
import com.frejdh.util.job.persistence.impl.h2.H2DaoService;
import com.frejdh.util.job.persistence.impl.memory.RuntimeDaoService;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public enum DaoPersistenceMode {
	H2(H2DaoService.class, "h2"),
	RUNTIME(RuntimeDaoService.class, "runtime", "in-memory", "memory", "internal"),
	CUSTOM(null, "custom");
	private final Class<? extends AbstractDaoServiceImpl> implementationClass;
	private final List<String> aliases;

	DaoPersistenceMode(Class<? extends AbstractDaoServiceImpl> implementation, String... aliases) {
		this.implementationClass = implementation;
		this.aliases = Arrays.asList(aliases);
	}

	public Class<? extends AbstractDaoServiceImpl> getImplementationClass() {
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

	public static Class<? extends AbstractDaoServiceImpl> toImplementationClass(String arg) {
		DaoPersistenceMode foundEnum = toEnum(arg);
		return foundEnum != null ? foundEnum.implementationClass : null;
	}

}
