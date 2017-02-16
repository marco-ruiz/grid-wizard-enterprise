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

package org.gwe.api;

import java.rmi.RemoteException;
import java.util.List;

import org.gwe.api.exceptions.GWEDomainException;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Nov 3, 2008
 */
public class Session4ClientAPIEnhancer {

	private Session4ClientAPI session;
	
	public Session4ClientAPIEnhancer(Session4ClientAPI session) {
	    this.session = session;
    }
	
	public Session4ClientAPI getSession() {
    	return session;
    }

	//=============
	// EASIER APIs
	//=============
	/* (non-Javadoc)
     * @see org.gwe.api.ISession4ClientAPI#doOrderBasedOperation(org.gwe.api.ClientOrderBasedOperation, int)
     */
	public String doOrderBasedOperation(ClientOrderBasedOperation oper, int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException, GWEDomainException {
		if (oper == null) return "No operation specified!";
		switch (oper) {
			case PAUSE: 	pauseOrder(orderId);  break;
			case RESUME: 	resumeOrder(orderId); break;
			case ABORT: 	abortOrder(orderId);  break;
			case DELETE: 	deleteOrder(orderId); break;
		}
		return "Operation '" + oper.getId() + "' executed sucessfully over order " + orderId;
	}
	
	/* (non-Javadoc)
     * @see org.gwe.api.ISession4ClientAPI#doOrderBasedQuery(org.gwe.api.ClientOrderBasedQuery, int, int)
     */
	public String doOrderBasedQuery(ClientOrderBasedQuery oper, int orderId, int jobNum) throws ServerAPIConnectionException, RemoteException, GWEDomainException {
		try {
	        return (oper == null) ? "No query specified." : oper.getOutput(getOrderBasedQueryResultModel(oper, orderId, jobNum));
        } catch (ValidationException e) {
        	return e.getMessage();
        }
	}

	private Object getOrderBasedQueryResultModel(ClientOrderBasedQuery oper, int orderId, int jobNum) throws ServerAPIConnectionException, RemoteException, GWEDomainException, ValidationException {
		switch (oper) {
			case GET_ORDER: 	return getOrderDetails(orderId, false);
			case GET_JOB: 		return getJobDetails(orderId, jobNum);
			case LIST_ORDERS: 	return getOrdersList(false);
			case LIST_JOBS: 	return getOrderDetails(orderId, true);
			case STREAM_RESULT: 
				JobInfo job = getJobDetails(orderId, jobNum);
				return (job != null && job.getExecution() != null) ? 
						job.getExecution().getRequestResult() : null;
		}
		return null;
	}

	public void abortOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.abortOrder(orderId);
    }

	public void deleteOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.deleteOrder(orderId);
    }

	public JobInfo getJobDetails(int orderId, int jobNum) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
	    return session.getJobDetails(orderId, jobNum);
    }

	public OrderInfo getOrderDetails(int orderId, boolean includeJobDetails) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
	    return session.getOrderDetails(orderId, includeJobDetails);
    }

	public List<OrderInfo> getOrders(String description) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
	    return session.getOrders(description);
    }

	public List<OrderInfo> getOrdersList(boolean includeJobDetails) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
	    return session.getOrdersList(includeJobDetails);
    }

	public String getUsername() {
	    return session.getUsername();
    }

	public void pauseOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.pauseOrder(orderId);
    }

	public OrderInfo queueOrder(OrderInfo order) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
	    return session.queueOrder(order);
    }

	public List<String> previewOrder(OrderInfo order) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException, Exception {
	    return session.previewOrder(order);
    }

	public void resumeOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.resumeOrder(orderId);
	}

	//==============
	// Admin Tasks
	//==============
	public void configureDaemon(HeadResourceInfo daemonInfo) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.configureDaemon(daemonInfo);
    }

	public long getDaemonTime() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
	    return 0;
    }

	public void shutdownDaemon() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
    }
	
	public void updateConfig(DaemonConfigDesc config) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		session.updateConfig(config);
    }

	public void swapPriorities(int orderId, int orderId2) throws PasswordMismatchException, RemoteException, ServerAPIConnectionException {
		session.swapPriorities(orderId, orderId2);
    }

	public void cleanupDisposedAllocations() throws PasswordMismatchException, RemoteException, ServerAPIConnectionException {
		session.cleanupDisposedAllocations();
    }
}
