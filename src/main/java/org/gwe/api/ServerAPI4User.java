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

import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.security.AccountInfo;

/**
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public interface ServerAPI4User extends SecuredServerAPI {

	//==============
	// Admin Tasks
	//==============
	public long getDaemonTime(AccountInfo auth) throws RemoteException, PasswordMismatchException;
	
	public void configureDaemon(AccountInfo auth, HeadResourceInfo daemonInfo) throws RemoteException, PasswordMismatchException;

	public void shutdownDaemon(AccountInfo auth) throws RemoteException, PasswordMismatchException;

	public void updateConfig(AccountInfo sessionId, DaemonConfigDesc config) throws RemoteException, PasswordMismatchException;

	//========
	// Orders
	//========
	public List<OrderInfo> getOrdersDefined(AccountInfo auth) throws RemoteException, PasswordMismatchException;

	public OrderInfo queueOrder(AccountInfo auth, OrderInfo order) throws RemoteException, PasswordMismatchException;

	public List<String> previewOrder(AccountInfo auth, OrderInfo order) throws RemoteException, PasswordMismatchException, Exception;

	public void pauseOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException;

	public void resumeOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException;

	public void abortOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException;

	public void deleteOrder(AccountInfo auth, int orderId) throws RemoteException, PasswordMismatchException;

	public void updateOrder(AccountInfo auth, int orderId, OrderInfo order) throws RemoteException;

	// Change priority, scheduling parameters, allocation percentages, etc
//	public void updateOrder(int orderId, OrderInfo order) throws RemoteException;
	
	//==================
	// Order Monitoring
	//==================
	public List<OrderInfo> getOrdersByDescription(AccountInfo auth, String description) throws RemoteException, PasswordMismatchException;

	public OrderInfo getOrderDetails(AccountInfo auth, int orderId, boolean includeJobs) throws RemoteException, PasswordMismatchException;

	public JobInfo getJobDetails(AccountInfo auth, int orderId, int jobNum) throws RemoteException, PasswordMismatchException;

	public List<OrderInfo> getOrdersList(AccountInfo auth, boolean includeJobs) throws RemoteException, PasswordMismatchException;

	public void swapPriorities(AccountInfo sessionId, int orderId1, int orderId2) throws RemoteException, PasswordMismatchException;

	public void cleanupDisposedAllocations(AccountInfo sessionId) throws RemoteException, PasswordMismatchException;

/*
	 public OrderStatus getOrderStatus(int orderId) { 
		 // Where each task/job have been allocated and the status of them:
		 // in queue, compute resource working on it (through agent) or completed (successfully, with errors, etc)
		 return null;
	 }
	 
	 public List<ComputeResourceInfo> getAgents() { 
		 // Where each task/job have been allocated and the status of them (scheduled in job manager, agent working on it, completed, etc) 
		 return null;
	 }
*/
}
