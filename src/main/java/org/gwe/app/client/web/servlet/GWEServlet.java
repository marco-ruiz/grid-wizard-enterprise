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

package org.gwe.app.client.web.servlet;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.web.GWEServletContext;
import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.view.MainVelocityTemplate;
import org.gwe.utils.VelocityUtils;


/**
 * @author Marco Ruiz
 * @since Dec 13, 2008
 */
public abstract class GWEServlet extends HttpServlet {

	private String contextRelativePath;
	private String template;

	public GWEServlet(String context) {
		this.contextRelativePath = context;
	}
	
	public void setMainTemplate(MainVelocityTemplate mainTemplate) throws IOException {
		this.template = mainTemplate.createPageTemplate(getTemplateName());
    }

	protected ClientConfig getConfig() {
    	return (ClientConfig) getServletContext().getAttribute(GWEServletContext.GWE_CONFIGURATION);
    }

	protected String getTemplateName() {
		return contextRelativePath + ".html";
	}
	
	public String getContextRelativePath() {
    	return contextRelativePath;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(createPage(request));
    }

	private String createPage(HttpServletRequest request) throws ServletException {
	    try {
	        return VelocityUtils.mergeThrowingExceptions(createPageModel(request), template);
        } catch (Exception e) {
        	throw new ServletException("Problems parsing velocity template", e);
        }
    }

	/**
	 * Default implementation
	 * 
	 * @param request
	 * @return
	 */
	protected PageModel createPageModel(HttpServletRequest request) {
		return new PageModel(request, getConfig());
	}
	
	public static void main(String[] args) throws IOException {
		GWEServlet servlet = new GWEServlet("macro") {};
		servlet.setMainTemplate(new MainVelocityTemplate());
		String page = VelocityUtils.evaluate(servlet.template);
		FileOutputStream fos = new FileOutputStream("/Users/admin/work/eclipse-ws/gwe-core/tmp/web/test.html");
		fos.write(page.getBytes());
		fos.close();
	}
}



