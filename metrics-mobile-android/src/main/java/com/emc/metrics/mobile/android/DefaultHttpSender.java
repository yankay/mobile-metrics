package com.emc.metrics.mobile.android;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.emc.metrics.mobile.Metrics;
import com.emc.metrics.mobile.Metrics.Metric;
import com.emc.metrics.mobile.Metrics.Metric.ValueType;
import com.emc.metrics.mobile.Metrics.MetricList;
import com.yammer.metrics.core.MetricName;

public class DefaultHttpSender implements HttpSender {

	List<Metric> buf = new ArrayList<Metric>();

	private URI uri;

	public DefaultHttpSender(URI uri) {
		this.uri = uri;
	}

	@Override
	public void sendInt(long timestamp, MetricName name, String valueName,
			long value) {
		Metric.Builder m = Metrics.Metric.newBuilder();
		m.setTimestamp(timestamp);
		m.setName(metricNameBuilder(name));
		m.setValueName(valueName);
		m.setValueType(ValueType.INT);
		m.setIntValue(value);
		buf.add(m.build());
	}

	@Override
	public void sendFloat(long timestamp, MetricName name, String valueName,
			double value) {
		Metric.Builder m = Metrics.Metric.newBuilder();
		m.setTimestamp(timestamp);
		m.setName(metricNameBuilder(name));
		m.setValueName(valueName);
		m.setValueType(ValueType.FLOAT);
		m.setFloatValue(value);
		buf.add(m.build());

	}

	@Override
	public void sendString(long timestamp, MetricName name, String valueName,
			String value) {
		Metric.Builder m = Metrics.Metric.newBuilder();
		m.setTimestamp(timestamp);
		m.setName(metricNameBuilder(name));
		m.setValueName(valueName);
		m.setValueType(ValueType.STRING);
		m.setStringValue(value);
		buf.add(m.build());

	}

	private com.emc.metrics.mobile.Metrics.Metric.MetricName.Builder metricNameBuilder(
			MetricName name) {
		com.emc.metrics.mobile.Metrics.Metric.MetricName.Builder b = com.emc.metrics.mobile.Metrics.Metric.MetricName
				.newBuilder();
		b.setGroup(name.getGroup());
		b.setType(name.getType());
		b.setName(name.getName());
		if (name.getScope() != null)
			b.setScope(b.getScope());
		return b;
	}

	@Override
	public void flush() {
		HttpURLConnection conn = null;
		try {
			try {
				conn = (HttpURLConnection) uri.toURL().openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setUseCaches(false);
				conn.connect();
				MetricList content = MetricList.newBuilder().addAllMetric(buf)
						.build();
				content.writeTo(conn.getOutputStream());
				conn.getOutputStream().close();
				int code = conn.getResponseCode();
				if (code != 200) {
					System.err.print("error flush, http response " + code);
				}
			} finally {
				if (conn != null)
					conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
