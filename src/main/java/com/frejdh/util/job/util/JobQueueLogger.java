package com.frejdh.util.job.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobQueueLogger {

	private static final String LOGGER_NAME = JobQueueLogger.class.getPackage().getName();

	private static final Logger LOGGER = initLogger();

	private static Logger initLogger() {
		Level loggingLevel = Level.FINEST;
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT %1$tL] [%4$-7s] %5$s %n");

		Logger logger = Logger.getLogger(LOGGER_NAME);
		logger.setLevel(loggingLevel);
		for (Handler handler : logger.getHandlers()) {
			handler.setLevel(loggingLevel);
		}

		return logger;
	}

	public static Logger getLogger() {
		return LOGGER;
	}
}
