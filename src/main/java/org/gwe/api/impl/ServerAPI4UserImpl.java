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

package org.gwe.api.impl;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPI4User;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.daemon.domain.UserDomain;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.security.AccountInfo;

/**
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public class ServerAPI4UserImpl extends SecuredServerAPIImpl<ServerAPI4User, UserDomain> implements ServerAPI4User {

	private static Log log = LogFactory.getLog(ServerAPI4UserImpl.class);
	
	public void configureDaemon(AccountInfo auth, HeadResourceInfo daemonInfo) throws RemoteException, PasswordMismatchException {
//		domain.configureDaemon(daemonInfo);
	}

	public void shutdownDaemon(AccountInfo auth) throws RemoteException, PasswordMismatchException {}
	

	//==============
	// User Profile
	//==============
	public long getDaemonTime(AccountInfo auth) throws RemoteException, PasswordMismatchException {
		return System.currentTimeMillis();
	}
	
	public void updateConfig(AccountInfo sessionId, DaemonConfigDesc config) throws RemoteException, PasswordMismatchException {
		try {
	        domain.updateConfig(config);
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        log.warn(e);
        }
    }

	//================
	// Orders Control
	//================
	public List<OrderInfo> getOrdersDefined(AccountInfo auth) throws RemoteException, PasswordMismatchException {
		return domain.getOrdersDefined();
	}

	public OrderInfo queueOrder(AccountInfo auth, OrderInfo order) throws RemoteException, PasswordMismatchException {
		order = domain.persistOrder(order);
		domain.queueOrder(order);
        log.info("Request to queue an order received: " + order);
        return order;
	}

	public List<String> previewOrder(AccountInfo auth, OrderInfo order) throws RemoteException, PasswordMismatchException, Exception {
		return domain.previewOrder(order);
	}

	public void pauseOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException {
		setPause(orderId, true);
	}

	public void resumeOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException {
		setPause(orderId, false);
	}
	
	private void setPause(int orderId, boolean pause) throws PasswordMismatchException {
		domain.pauseOrder(orderId, pause);
	}

	public void abortOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException {
		domain.abortOrder(orderId);
	}

	public void deleteOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException {
		domain.deleteOrder(orderId);
    }

	public void updateOrder(AccountInfo auth, int orderId, OrderInfo order) throws RemoteException {
		// Change priority, scheduling parameters, allocation percentages, etc
		log.info("Request to update an order received: " + order);
		if (true) return;
	}

	//==================
	// Order Monitoring
	//==================
	public List<OrderInfo> getOrdersByDescription(AccountInfo auth, String description) throws RemoteException, PasswordMismatchException {
		return domain.getOrdersByDescription(description);
	}

	public OrderInfo getOrderDetails(AccountInfo auth, int orderId, boolean includeJobs) throws RemoteException, PasswordMismatchException {
		return domain.getOrder(orderId, includeJobs);
	}

	public List<OrderInfo> getOrdersList(AccountInfo auth, boolean includeJobs) throws RemoteException, PasswordMismatchException {
		return domain.getOrdersList(includeJobs);
	}

	public JobInfo getJobDetails(AccountInfo auth, int orderId, int jobNum) throws RemoteException, PasswordMismatchException {
		JobInfo result = domain.getJobDetails(orderId, jobNum);
		result.setExecutions(domain.getExecutionDetails(orderId, jobNum));
		return result;
	}

	public void swapPriorities(AccountInfo auth, int orderId, int orderId2) throws RemoteException, PasswordMismatchException {
		domain.swapPriorities(orderId, orderId2);
    }

	public void cleanupDisposedAllocations(AccountInfo sessionId) throws RemoteException, PasswordMismatchException {
		domain.cleanupDisposedAllocations();
    }

	// Inspector
	/*
	 * public WorkOrderStatus getWorkOrderStatus() { // Where each task/job have been allocated and the status
	 * of them (scheduled in job manager, agent working on it, completed, etc) }
	 * 
	 * public WorkOrderStatus getAgents() { // Where each task/job have been allocated and the status of them
	 * (scheduled in job manager, agent working on it, completed, etc) }
	 */
}
