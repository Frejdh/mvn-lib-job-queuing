package com.frejdh.util.job.persistence;

import com.frejdh.util.job.persistence.impl.h2.H2DaoService;
import com.frejdh.util.job.persistence.impl.memory.InMemoryDaoService;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

enum PredefinedDaoImplementation {
	H2(H2DaoService.class, "h2"), MEMORY(InMemoryDaoService.class, "in-memory", "memory", "internal", "runtime");

	private final Class<? extends AbstractDaoService> implementationClass;
	private final List<String> caseInsensitiveArgs;

	PredefinedDaoImplementation(Class<? extends AbstractDaoService> implementation, String... caseInsensitiveArgs) {
		this.implementationClass = implementation;
		this.caseInsensitiveArgs = Arrays.asList(caseInsensitiveArgs);
	}

	public Class<? extends AbstractDaoService> getImplementationClass() {
		return implementationClass;
	}

	public List<String> getCaseInsensitiveArgs() {
		return caseInsensitiveArgs;
	}

	public static PredefinedDaoImplementation toEnum(String arg) {
		if (arg == null) {
			return null;
		}

		return Arrays.stream(values())
				.filter(daoEnum -> daoEnum.caseInsensitiveArgs.contains(arg.toLowerCase(Locale.ROOT)))
				.findFirst().orElse(null);
	}

	public static Class<? extends AbstractDaoService> toImplementationClass(String arg) {
		PredefinedDaoImplementation foundEnum = toEnum(arg);
		return foundEnum != null ? foundEnum.implementationClass : null;
	}

}
