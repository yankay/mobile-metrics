package com.emc.metrics.mobile.android;

import java.net.URL;

import com.yammer.metrics.core.MetricName;

public class DefaultHttpSender implements HttpSender{

	public DefaultHttpSender(URL uri) {
	}

	@Override
	public void sendInt(long timestamp, MetricName name, String valueName,
			long value) {
		
	}

	@Override
	public void sendFloat(long timestamp, MetricName name, String valueName,
			double value) {
		
	}

	@Override
	public void sendString(long timestamp, MetricName name, String valueName,
			String value) {
		
	}

	@Override
	public void flush() {
		
	}

}
