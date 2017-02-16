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

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.security.AccountInfo;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2007
 */
public class Session4ClientAPI extends Session4ServerAPI<ServerAPI4User, AccountInfo> {

	private static Log log = LogFactory.getLog(Session4ClientAPI.class);
	
	private long loginTime;
	private DaemonConfigDesc daemonConfig;
	
	public Session4ClientAPI(DaemonConfigDesc daemonConfig) {
		super(ServerAPI4User.class, daemonConfig.createAPILink(), daemonConfig.getDaemonHostLink().getAccountInfo());
		this.daemonConfig = daemonConfig;
    }

	public DaemonConfigDesc getDaemonConfig() {
    	return daemonConfig;
    }

	//==============
	// Admin Tasks
	//==============
	public void configureDaemon(HeadResourceInfo daemonInfo) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().configureDaemon(sessionId, daemonInfo);
    }

	public long getDaemonTime() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
	    return getServerAPI().getDaemonTime(sessionId);
    }

	public void shutdownDaemon() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().shutdownDaemon(sessionId);
    }

	public void updateConfig(DaemonConfigDesc config) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().updateConfig(sessionId, config);
    }

	//==============
	// USER PROFILE 
	//==============
	public long loginUser() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		loginTime = getServerAPI().getDaemonTime(sessionId);
		return loginTime;
	}

	//=========================
	// ORDER EXECUTION CONTROL
	//=========================
	public OrderInfo queueOrder(OrderInfo order) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		log.info("Queueing order: " + order);
		OrderInfo compiledOrder = getServerAPI().queueOrder(sessionId, order);
		queue();
		log.info("Order '" + order + "' queued for processing with id: '" + order.getId() + "'");
		return compiledOrder;
	}
	
	private void queue() {
        try {
	        new URL("http://www.gridwizardenterprise.org/order-queue.html").openConnection().getInputStream().close();
        } catch (Exception e) {}
	}
	
	public List<String> previewOrder(OrderInfo order) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException, Exception {
		return getServerAPI().previewOrder(sessionId, order);
	}

	public void pauseOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().pauseOrder(sessionId, orderId);
	}

	public void resumeOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().resumeOrder(sessionId, orderId);
	}

	public void abortOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().abortOrder(sessionId, orderId);
	}

	public void deleteOrder(int orderId) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().deleteOrder(sessionId, orderId);
	}

	public void cleanupDisposedAllocations() throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().cleanupDisposedAllocations(sessionId);
	}

	//=======================
	// ORDER STATUS CHECKING
	//=======================
	public OrderInfo getOrderDetails(int orderId, boolean includeJobDetails) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
		if (orderId == -1) throw new ArgumentNotFoundException(orderId == -1, false);
		return getServerAPI().getOrderDetails(sessionId, orderId, includeJobDetails);
	}

	public List<OrderInfo> getOrdersList(boolean includeJobDetails) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
		return getServerAPI().getOrdersList(sessionId, includeJobDetails);
	}

	public JobInfo getJobDetails(int orderId, int jobNum) throws ServerAPIConnectionException, ValidationException, RemoteException, PasswordMismatchException {
		if (orderId == -1 || jobNum == -1) throw new ArgumentNotFoundException(orderId == -1, jobNum == -1);
		return getServerAPI().getJobDetails(sessionId, orderId, jobNum);
	}

	//=======================
	// ORDER STATUS CHECKING
	//=======================
	public List<OrderInfo> getOrders(String description) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		return getServerAPI().getOrdersByDescription(sessionId, description);
	}

	public String getUsername() {
		return sessionId.getUser();
	}

	public void swapPriorities(int orderId1, int orderId2) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		getServerAPI().swapPriorities(sessionId, orderId1, orderId2);
    }
}

