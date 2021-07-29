package com.frejdh.util.job.persistence.config;

import com.frejdh.util.job.persistence.AbstractDaoService;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;


@Getter
public class DaoPersistence {

	private final DaoPersistenceMode mode;
	private final Class<? extends AbstractDaoService> implementationClass;

	public DaoPersistence(@NotNull DaoPersistenceMode mode) {
		if (mode == DaoPersistenceMode.CUSTOM) {
			throw new IllegalArgumentException("CUSTOM mode requires a provided implementation class. " +
					"Please use the other constructor instead for " + this.getClass().getSimpleName());
		}
		this.mode = mode;
		this.implementationClass = mode.getImplementationClass();
	}

	public DaoPersistence(@NotNull Class<? extends AbstractDaoService> implementationClass) {
		this.mode = DaoPersistenceMode.CUSTOM;
		this.implementationClass = implementationClass;
	}

}

