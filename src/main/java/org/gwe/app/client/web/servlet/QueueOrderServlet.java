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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.List;

import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.client.web.request.Operation;
import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.request.Param;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.gwe.utils.rex.REXException;

/**
 * @author Marco Ruiz
 * @since Dec 13, 2008
 */
public class QueueOrderServlet extends GWEClusterSelectedServlet {

	public QueueOrderServlet() { super("queue"); }

    protected void specificPageModelPopulation(PageModel pageModel, Session4ClientAPIEnhancer session) {
    	String p2el = pageModel.getParam(Param.P2EL);
    	if (p2el == null || "".equals(p2el)) return;
    	Operation mode = pageModel.getOperation();
		try {
	        POrderDescriptor<Serializable> descriptor = (POrderDescriptor) getConfig().createOrderDescriptor(p2el);
	        switch (mode) {
	        	case PREVIEW:
		        	pageModel.addIdentifiedObject(Param.OPER, getParsedP2EL(descriptor));
		    		preview(pageModel, session, descriptor);
	        		break;
	        	case QUEUE:
		    		queue(pageModel, session, descriptor);
	        		break;
	        }
        } catch (REXException e) {
        	// TODO: handle
        }
    }

	private String getParsedP2EL(POrderDescriptor<Serializable> descriptor) {
		try {
			return descriptor.getP2ELStatementObj().toStringFormatted("", "\n");
		} catch (Exception e) {
			Writer result = new StringWriter();
			e.printStackTrace(new PrintWriter(result));
			return "Invalid P2EL statement: " + result.toString();
		}
    }
    
	private void queue(PageModel pageModel, Session4ClientAPIEnhancer session, POrderDescriptor<Serializable> descriptor) {
	    try {
			OrderInfo order = new OrderInfo(descriptor);
	        order = session.queueOrder(order);
			pageModel.addIdentifiedObject(Param.P2EL, order);
			pageModel.setParam(Param.P2EL, null);
        } catch (PasswordMismatchException e) {
        	// TODO: handle
        } catch (RemoteException e) {
        	// TODO: handle
        } catch (ServerAPIConnectionException e) {
        	// TODO: handle
        }
    }

	private void preview(PageModel pageModel, Session4ClientAPIEnhancer session, POrderDescriptor<Serializable> descriptor) {
	    try {
			OrderInfo order = new OrderInfo(descriptor);
	        List<String> commands = session.previewOrder(order);
			pageModel.addIdentifiedObject(Param.COMMANDS, commands);
        } catch (PasswordMismatchException e) {
        	// TODO: handle
        } catch (RemoteException e) {
        	// TODO: handle
        } catch (ServerAPIConnectionException e) {
        	// TODO: handle
        } catch (Exception e) {
        	// TODO: handle
        }
    }
}



