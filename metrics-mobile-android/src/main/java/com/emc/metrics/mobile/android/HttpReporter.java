package com.emc.metrics.mobile.android;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Metered;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricProcessor;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Sampling;
import com.yammer.metrics.core.Summarizable;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;

/**
 * A simple reporters which prints out application metrics to a
 * {@link PrintStream} periodically.
 */
public class HttpReporter extends AbstractPollingReporter implements
		MetricProcessor<Long> {

	/**
	 * Enables the console reporter for the default metrics registry, and causes
	 * it to print to STDOUT with the specified period.
	 * 
	 * @param period
	 *            the period between successive outputs
	 * @param unit
	 *            the time unit of {@code period}
	 */
	public static void enable(long period, TimeUnit unit, URL uri) {
		enable(Metrics.defaultRegistry(), period, unit, new DefaultHttpSender(
				uri));
	}

	/**
	 * Enables the console reporter for the given metrics registry, and causes
	 * it to print to STDOUT with the specified period and unrestricted output.
	 * 
	 * @param metricsRegistry
	 *            the metrics registry
	 * @param period
	 *            the period between successive outputs
	 * @param unit
	 *            the time unit of {@code period}
	 */
	public static void enable(MetricsRegistry metricsRegistry, long period,
			TimeUnit unit, HttpSender sender) {
		final HttpReporter reporter = new HttpReporter(metricsRegistry, sender,
				MetricPredicate.ALL);
		reporter.start(period, unit);
	}

	private final MetricPredicate predicate;
	private final Clock clock;
	private HttpSender sender;

	/**
	 * Creates a new {@link HttpReporter} for the default metrics registry, with
	 * unrestricted output.
	 * 
	 * @param out
	 *            the {@link PrintStream} to which output will be written
	 */
	public HttpReporter(HttpSender sender) {
		this(Metrics.defaultRegistry(), sender, MetricPredicate.ALL);
	}

	/**
	 * Creates a new {@link HttpReporter} for a given metrics registry.
	 * 
	 * @param metricsRegistry
	 *            the metrics registry
	 * @param out
	 *            the {@link PrintStream} to which output will be written
	 * @param predicate
	 *            the {@link MetricPredicate} used to determine whether a metric
	 *            will be output
	 */
	public HttpReporter(MetricsRegistry metricsRegistry, HttpSender sender,
			MetricPredicate predicate) {
		this(metricsRegistry, sender, predicate, Clock.defaultClock());
	}

	/**
	 * Creates a new {@link HttpReporter} for a given metrics registry.
	 * 
	 * @param metricsRegistry
	 *            the metrics registry
	 * @param out
	 *            the {@link PrintStream} to which output will be written
	 * @param predicate
	 *            the {@link MetricPredicate} used to determine whether a metric
	 *            will be output
	 * @param clock
	 *            the {@link com.yammer.metrics.core.Clock} used to print time
	 * @param timeZone
	 *            the {@link TimeZone} used to print time
	 * @param locale
	 *            the {@link Locale} used to print values
	 */
	public HttpReporter(MetricsRegistry metricsRegistry, HttpSender sender,
			MetricPredicate predicate, Clock clock) {
		super(metricsRegistry, "http-reporter");
		this.sender = sender;
		this.predicate = predicate;
		this.clock = clock;
	}

	@Override
	public void run() {
		try {
			final long epoch = clock.time() / 1000;
			for (Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry()
					.groupedMetrics(predicate).entrySet()) {
				for (Entry<MetricName, Metric> subEntry : entry.getValue()
						.entrySet()) {
					final Metric metric = subEntry.getValue();
					if (metric != null) {
						try {
							metric.processWith(this, subEntry.getKey(), epoch);
						} catch (Exception ignored) {
							System.err.println("ignored metric processWith");
						}
					}
				}
			}
			sender.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processGauge(MetricName name, Gauge<?> gauge, Long epoch)
			throws IOException {
		sendObjToGraphite(epoch, name, "value", gauge.value());
	}

	public void processCounter(MetricName name, Counter counter, Long epoch)
			throws IOException {
		sendInt(epoch, name, "count", counter.count());
	}

	public void processMeter(MetricName name, Metered meter, Long epoch)
			throws IOException {
		sendInt(epoch, name, "count", meter.count());
		sendFloat(epoch, name, "meanRate", meter.meanRate());
		sendFloat(epoch, name, "1MinuteRate", meter.oneMinuteRate());
		sendFloat(epoch, name, "5MinuteRate", meter.fiveMinuteRate());
		sendFloat(epoch, name, "15MinuteRate", meter.fifteenMinuteRate());
	}

	public void processHistogram(MetricName name, Histogram histogram,
			Long epoch) throws IOException {
		sendSummarizable(epoch, name, histogram);
		sendSampling(epoch, name, histogram);
	}

	@Override
	public void processTimer(MetricName name, Timer timer, Long epoch)
			throws IOException {
		processMeter(name, timer, epoch);
		sendSummarizable(epoch, name, timer);
		sendSampling(epoch, name, timer);
	}

	protected void sendInt(long timestamp, MetricName name, String valueName,
			long value) {
		sender.sendInt(timestamp, name, valueName, value);
	}

	protected void sendFloat(long timestamp, MetricName name, String valueName,
			double value) {
		sender.sendFloat(timestamp, name, valueName, value);
	}

	protected void sendString(long timestamp, MetricName name,
			String valueName, String value) {
		sender.sendString(timestamp, name, valueName, value);
	}

	protected void sendObjToGraphite(long timestamp, MetricName name,
			String valueName, Object value) {
		if (value instanceof Long || value instanceof Integer) {
			sendInt(timestamp, name, valueName, (Long) value);
		} else if (value instanceof Double || value instanceof Float) {
			sendFloat(timestamp, name, valueName, (Double) value);
		} else {
			sendString(timestamp, name, valueName, value.toString());
		}
	}

	protected void sendSummarizable(long epoch, MetricName sanitizedName,
			Summarizable metric) throws IOException {
		sendFloat(epoch, sanitizedName, "min", metric.min());
		sendFloat(epoch, sanitizedName, "max", metric.max());
		sendFloat(epoch, sanitizedName, "mean", metric.mean());
		sendFloat(epoch, sanitizedName, "stddev", metric.stdDev());
	}

	protected void sendSampling(long epoch, MetricName sanitizedName,
			Sampling metric) throws IOException {
		final Snapshot snapshot = metric.getSnapshot();
		sendFloat(epoch, sanitizedName, "median", snapshot.getMedian());
		sendFloat(epoch, sanitizedName, "75percentile",
				snapshot.get75thPercentile());
		sendFloat(epoch, sanitizedName, "95percentile",
				snapshot.get95thPercentile());
		sendFloat(epoch, sanitizedName, "98percentile",
				snapshot.get98thPercentile());
		sendFloat(epoch, sanitizedName, "99percentile",
				snapshot.get99thPercentile());
		sendFloat(epoch, sanitizedName, "999percentile",
				snapshot.get999thPercentile());
	}

}
