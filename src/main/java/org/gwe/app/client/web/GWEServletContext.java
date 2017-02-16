/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gwe.app.client.web;

import java.io.IOException;

import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.config.XMLClientConfigReader;
import org.gwe.app.client.web.servlet.GWEServlet;
import org.gwe.app.client.web.servlet.ImageServlet;
import org.gwe.app.client.web.view.MainVelocityTemplate;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.CredentialNotFoundException;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2008
 */
public class GWEServletContext extends Context {

	public static final String GWE_CONFIGURATION = "GWE_CONFIGURATION_KEY";
	
	private static final String RESOURCE_PATH = IOUtils.concatenatePaths(ClientConfig.extractGWE_HOME(), "web");

	private MainVelocityTemplate mainTemplate;
	private ClientConfig config;

	public GWEServletContext(String confArg, HandlerContainer contexts, Class<? extends GWEServlet>... servletClasses) throws Exception {
		this(confArg, contexts);
		addServlet(new ServletHolder(new ImageServlet(IOUtils.concatenatePaths(RESOURCE_PATH, "img", "imageNotFound.png"))), "/imageServ");
		for (Class<? extends GWEServlet> servletClass : servletClasses) addGWEServlet(servletClass);
	}
	
	public GWEServletContext(String confArg, HandlerContainer contexts) throws IOException, CredentialNotFoundException {
		super(contexts, "/"); 
		mainTemplate = new MainVelocityTemplate();
		config = new ClientConfig(new XMLClientConfigReader(confArg));
		setAttribute(GWE_CONFIGURATION, config);
		setResourceBase(RESOURCE_PATH);
		setWelcomeFiles(new String[] {"index.html"});
		addServlet(new ServletHolder(new DefaultServlet()), "/");
	}
	
	public void addGWEServlet(Class<? extends GWEServlet> servletClass) throws Exception {
		GWEServlet servlet = servletClass.newInstance();
		servlet.setMainTemplate(mainTemplate);
		addServlet(new ServletHolder(servlet), "/" + servlet.getContextRelativePath());
	}
}
