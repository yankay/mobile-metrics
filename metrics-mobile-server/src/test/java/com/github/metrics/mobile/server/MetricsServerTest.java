package com.github.metrics.mobile.server;

import java.io.IOException;

import org.junit.Test;

import com.github.metrics.mobile.server.MetricsServer;

public class MetricsServerTest {

	@Test
	public void testStart() throws IOException, InterruptedException {
		MetricsServer server = new MetricsServer();
		server.start();
		Thread.sleep(Long.MAX_VALUE);
	}

}
