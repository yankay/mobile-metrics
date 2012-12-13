/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.github.metrics.mobile.server;

import java.io.IOException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Allows HTTP clients to communicate via the BOSH protocol with Vysper.
 * <p>
 * See http://xmpp.org/extensions/xep-0124.html and
 * http://xmpp.org/extensions/xep-0206.html
 * 
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class MetricsServer {

	protected int port = 8080;

	protected Server server;

	protected String contextPath = "/";

	/**
	 * Setter for the listen port
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Determines the context URI where the BOSH transport will be accessible.
	 * The default is as 'root context' under '/'.
	 * 
	 * @param contextPath
	 */
	public void setContextPath(String contextPath) {
		if (contextPath == null)
			contextPath = "/";
		this.contextPath = contextPath;
	}

	/**
	 * create a basic Jetty server including a connector on the configured port
	 * override in subclass to create a different kind of setup or to reuse an
	 * existing instance
	 * 
	 * @return
	 */
	protected Server createJettyServer() {
		Server server = new Server(port);
		return server;
	}

	/**
	 * create handler for BOSH. for a different handler setup, override in a
	 * subclass. for more than one handler, add them to a
	 * org.eclipse.jetty.server.handler.ContextHandlerCollection and return the
	 * collection
	 * 
	 * @return
	 */
	protected Handler createHandler() {
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);

		MetricsServlet servlet = new MetricsServlet();
		context.addServlet(new ServletHolder(servlet), "/");

		return context;
	}

	/**
	 * @throws IOException
	 * @throws RuntimeException
	 *             a wrapper of the possible {@link java.lang.Exception} that
	 *             Jetty can throw at start-up
	 */
	public void start() throws IOException {

		server = createJettyServer();
		Handler handler = createHandler();
		server.setHandler(handler);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			// logger.warn("Could not stop the Jetty server", e);
		}
	}
	

}
