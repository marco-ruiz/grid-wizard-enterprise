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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.app.client.web.request.Operation;
import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.request.Param;

/**
 * @author Marco Ruiz
 * @since Dec 13, 2008
 */
public class ClusterInfoServlet extends GWEClusterSelectedServlet {

	private static Log log = LogFactory.getLog(ClusterInfoServlet.class);

	public ClusterInfoServlet() { super("cluster"); }
	
	protected void specificPageModelPopulation(PageModel pageModel, Session4ClientAPIEnhancer session) {
    	Operation oper = pageModel.getOperation();
    	if (oper == Operation.CLEANUP) {
    		try {
	            session.cleanupDisposedAllocations();
            } catch (Exception e) {
            	log.warn("Problems encountered while trying to clean up disposed allocations workspace", e);
            }
    		return;
    	}

        Integer orderId = pageModel.getParam(Param.ORDER_ID);
		if (orderId == null) return;
		try {
	        switch (oper) {
//	        	case UP:
//	        	case DOWN:   session.swapPriorities(orderId, pageModel.getParam(Param.OTHER_ORDER_ID)); break;
	        	case PAUSE:  session.pauseOrder(orderId); break;
	        	case RESUME: session.resumeOrder(orderId); break;
	        	case DELETE: session.deleteOrder(orderId); break;
	        }
        } catch (Exception e) {
        	log.warn("Problems encountered while trying to " + oper + " order " + orderId, e);
        }
    }
}
