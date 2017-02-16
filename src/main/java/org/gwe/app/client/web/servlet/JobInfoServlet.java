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

import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.request.Param;

/**
 * @author Marco Ruiz
 * @since Dec 13, 2008
 */
public class JobInfoServlet extends GWEClusterSelectedServlet {
	
	public JobInfoServlet() { super("job"); }
	
	protected void specificPageModelPopulation(PageModel pageModel, Session4ClientAPIEnhancer session) {
		Integer orderId = pageModel.getParam(Param.ORDER_ID);
		Integer jobNum = pageModel.getParam(Param.JOB_NUM);
		if (orderId == null || jobNum == null) return;
		try {
			pageModel.addIdentifiedObject(Param.JOB_NUM, session.getJobDetails(orderId, jobNum));
		} catch (Exception e) {
		}
	}
}
