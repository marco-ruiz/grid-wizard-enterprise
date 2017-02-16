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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.client.GWEASCIILogo;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.web.servlet.ClusterInfoServlet;
import org.gwe.app.client.web.servlet.GridInfoServlet;
import org.gwe.app.client.web.servlet.JobInfoServlet;
import org.gwe.app.client.web.servlet.OrderInfoServlet;
import org.gwe.app.client.web.servlet.QueueOrderServlet;
import org.gwe.app.client.web.view.MainVelocityTemplate;
import org.gwe.utils.IOUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author Marco Ruiz
 * @since Jan 25, 2008
 */
public class WebServerApp {

	private static Log log = LogFactory.getLog(WebServerApp.class);

	public static void main(String[] args) throws Exception {
    	String jettyLogName = IOUtils.concatenatePaths(ClientConfig.extractGWE_HOME(), "jetty.log");
		System.setErr(new PrintStream(new File(jettyLogName)));
		int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 8080;
		String confArg = (args.length >= 2) ? args[1] : "";
		WebServerApp app = new WebServerApp(port);
		app.startWebApp(confArg);
    }
	
    private int port;
	private Server server;
	private Context context;

    public WebServerApp(int port) {
    	this.port = port;
    }

	private void startWebApp(String confArg) throws Exception {
		header();

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		context = new GWEServletContext(confArg, contexts, 
				QueueOrderServlet.class, 
				GridInfoServlet.class, 
				ClusterInfoServlet.class, 
				OrderInfoServlet.class, 
				JobInfoServlet.class);
		
		System.out.println("Launching...");
	    server = new Server(port);
		context.addServlet(new ServletHolder(new ShutdownServlet()), "/shutdown");
		server.addHandler(contexts);
		server.addHandler(new DefaultHandler());
		
		server.setHandler(context);
		server.start();

		footer();
//		openURL(getURL());
		server.join();
	}

	private void header() {
	    System.out.println("==============================================================");
		System.out.println(GWEASCIILogo.prefixLogo(GWEASCIILogo.GWE, "\t") + "\n");
		System.out.println("\tWelcome to the GWE 'Web Control Panel' Application");
		System.out.println("\tURL: " + getURL() + " \n");
		System.out.println("\n==============================================================\n");
    }

	private void footer() {
	    System.out.println("Web Control Panel Application Launched!");
		System.out.println("Point your browser to " + getURL() + " to access it...");
    }

	private String getURL() {
	    return "'http://localhost:" + port + "'";
    }

	class ShutdownServlet extends HttpServlet {

		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			response.setContentType("text/html");
	        response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(MainVelocityTemplate.readTemplateFile("shutdown.html"));
			launchAsyncStop();
	    }

		private void launchAsyncStop() {
			new Thread(new Runnable() {
				public void run() {
					try {
	                    Thread.sleep(2000);
                    } catch (InterruptedException e) {}
			        try {
			    		context.setShutdown(true);
			            server.stop();
		            } catch (Exception e) {}
		            System.exit(0);
                }
			}).start();
        }
	}
}

