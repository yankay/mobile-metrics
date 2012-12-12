package com.emc.metrics.mobile.server;

import java.io.IOException;

import org.junit.Test;

public class MetricsServerTest {

	@Test
	public void testStart() throws IOException, InterruptedException {
		MetricsServer server = new MetricsServer();
		server.start();
		Thread.sleep(Long.MAX_VALUE);
	}

}
