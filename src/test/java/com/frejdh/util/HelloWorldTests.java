package com.frejdh.util;

import org.junit.Test;
import java.util.logging.Logger;

public class HelloWorldTests {

	private Logger logger = Logger.getLogger("HelloWorldTests");

	@Test
	public void test() {
		logger.info("Hello World: Test Edition!");
	}
}
