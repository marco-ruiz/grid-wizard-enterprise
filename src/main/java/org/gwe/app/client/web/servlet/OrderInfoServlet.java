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
import org.gwe.p2elv2.model.PStatement;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.rex.REXParser;

/**
 * @author Marco Ruiz
 * @since Dec 13, 2008
 */
public class OrderInfoServlet extends GWEClusterSelectedServlet {

	private static final String P2EL_STATEMENT = "p2elStatement";

	public OrderInfoServlet() { super("order"); }

	protected void specificPageModelPopulation(PageModel pageModel, Session4ClientAPIEnhancer session) {
		Integer orderId = pageModel.getParam(Param.ORDER_ID);
		if (orderId == null) return;
		try {
			OrderInfo order = session.getOrderDetails(orderId, true);
			pageModel.addIdentifiedObject(Param.ORDER_ID, order);
			pageModel.put(P2EL_STATEMENT, getP2ELStatement(order));
		} catch (Exception e) {
		}
    }

	private PStatement getP2ELStatement(OrderInfo order) {
	    try {
			return REXParser.createModel(PStatement.class, ((POrderDescriptor)order.getDescriptor()).getP2ELStatement(), true);
        } catch (REXException e) {
        }
        return null;
    }
}

