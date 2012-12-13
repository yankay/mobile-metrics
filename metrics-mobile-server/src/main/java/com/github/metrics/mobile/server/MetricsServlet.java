package com.github.metrics.mobile.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.metrics.mobile.Metrics;
import com.github.metrics.mobile.Metrics.Metric;
import com.github.metrics.mobile.Metrics.MetricList;

public class MetricsServlet extends HttpServlet {
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Metrics.MetricList list = MetricList.parseFrom(req.getInputStream());
		for (Metric m : list.getMetricList()) {
			System.out.println(m.getName().getGroup() + "."
					+ m.getName().getName() + " : " + m.getStringValue());
		}
	}

}
