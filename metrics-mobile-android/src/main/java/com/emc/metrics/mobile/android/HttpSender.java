package com.emc.metrics.mobile.android;

import com.yammer.metrics.core.MetricName;

public interface HttpSender {

	void sendInt(long timestamp, MetricName name, String valueName, long value);

	void sendFloat(long timestamp, MetricName name, String valueName,
			double value);

	void sendString(long timestamp, MetricName name, String valueName,
			String value);

	void flush();

}
