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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.app.client.admin.InstallerException;
import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.request.Param;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Dec 15, 2008
 */
public abstract class GWEClusterSelectedServlet extends GWEServlet {

	public GWEClusterSelectedServlet(String context) {
	    super(context);
    }
	
	protected HeadResourceInfo getSelectedCluster(PageModel pageModel) {
	    return getConfig().getGrid().getHeadResource(pageModel.getParam(Param.CLUSTER_ID));
    }

    protected PageModel createPageModel(HttpServletRequest request) {
		PageModel pageModel = new PageModel(request, getConfig());
		
		pageModel.addIdentifiedObject(Param.CLUSTER_ID, getSelectedCluster(pageModel));
        try {
	        populatePageModel(pageModel);
        } catch (InstallerException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (ServerAPIConnectionException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	    return pageModel;
    }

	private void populatePageModel(PageModel pageModel) throws InstallerException, ServerAPIConnectionException {
	    Session4ClientAPIEnhancer session = getSelectedClusterSession(pageModel);
		pageModel.put("gweSession", session);
		specificPageModelPopulation(pageModel, session);
	    addOrderList(pageModel, session);
    }

	private void addOrderList(PageModel pageModel, Session4ClientAPIEnhancer session) {
	    HeadResourceInfo selectedCluster = getSelectedCluster(pageModel);
		if (selectedCluster != null) {
	        List<OrderInfo> ordersList = querySelectedClusterOrdersList(session);
	        selectedCluster.setOrdersList(ordersList);
        }
    }

	private Session4ClientAPIEnhancer getSelectedClusterSession(PageModel pageModel) throws InstallerException, ServerAPIConnectionException {
		HeadResourceInfo selectedCluster = getSelectedCluster(pageModel);
		Boolean installDaemonIfNeeded = pageModel.getParam(Param.INSTALL);
		BundleType installDaemonIfNeededBundle = BundleType.get(pageModel.getParam(Param.BUNDLE));
		return getConfig().getSessionsRepository().getSession(selectedCluster, installDaemonIfNeeded, installDaemonIfNeededBundle);
	}

	private List<OrderInfo> querySelectedClusterOrdersList(Session4ClientAPIEnhancer session) {
	    try {
	        return session.getOrdersList(false);
	    } catch (Exception e) {}
	    return null;
    }
	
	/**
	 * Callback method to populate page model with more contextual object models. Default: does nothing!
	 * 
	 * @param pageModel
	 * @param session
	 */
	protected void specificPageModelPopulation(PageModel pageModel, Session4ClientAPIEnhancer session) {}
}
