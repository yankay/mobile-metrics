package com.emc.metrics.mobile.android;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.reporting.ConsoleReporter;

public class HttpReporterTest {

	@Test
	public void testRun() throws InterruptedException, URISyntaxException {
		Metrics.newGauge(this.getClass(), "test", new Gauge<String>() {

			@Override
			public String value() {
				return "hello world";
			}
		});
		HttpReporter.enable(1, TimeUnit.SECONDS, new URI("http://localhost:8080"));
		Thread.sleep(Long.MAX_VALUE);
	}

}
