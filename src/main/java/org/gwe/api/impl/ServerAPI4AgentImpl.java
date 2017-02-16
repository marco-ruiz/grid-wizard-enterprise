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

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPI4Agent;
import org.gwe.api.ShutdownDaemonRequest;
import org.gwe.app.daemon.domain.AgentDomain;
import org.gwe.persistence.model.ComputeResourceInfo;
import org.gwe.persistence.model.order.DaemonRequest;

/**
 * 
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public class ServerAPI4AgentImpl extends PulsingServerAPIImpl<ServerAPI4Agent, AgentDomain> implements ServerAPI4Agent {

	private static Log log = LogFactory.getLog(ServerAPI4AgentImpl.class);

	//===========
	// API CALLS
	//===========
	public boolean reserveAllocation(int allocId, ComputeResourceInfo compRes) throws RemoteException {
		log.trace("Allocation " + allocId + " called back! - 'reserveAllocation' method");
		try {
			return domain.attachAllocation(allocId, compRes);
		} catch(Exception e) {
			disposeAllocation(allocId, e);
		}
		return false;
	}

	public DaemonRequest<?> getNextRequest(int allocId) throws RemoteException {
		log.trace("Allocation " + allocId + " called back! - 'getNextRequest' method");
		try {
			return domain.extractRequestFromNextJobAssignedBlocking(allocId);
		} catch(Exception e) {
			return disposeAllocation(allocId, e);
		}
	}

	public DaemonRequest<?> getNextRequestAgain(int allocId) throws RemoteException {
		log.trace("Allocation " + allocId + " called back! - 'getNextRequestAgain' method");
		try {
			return domain.getCurrentProcessingExecution(allocId);
		} catch(Exception e) {
			return disposeAllocation(allocId, e);
		}
	}

	public boolean reportRequestCompletion(int allocId, String execId, Serializable result) throws RemoteException {
		log.trace("Allocation " + allocId + " called back! - 'reportRequestCompletion' method");
		try {
			domain.cleanAllocAndFinalizeJobAsync(allocId, execId, result);
			return true;
		} catch(Exception e) {
			disposeAllocation(allocId, e);
		}
		return false;
	}

	public boolean reportAgentProblem(int allocId, Exception exception) throws RemoteException {
		log.trace("Allocation " + allocId + " called back! - 'reportAgentProblem' method");
		disposeAllocation(allocId, exception);
	    return true;
    }

	//==================================================================
	// DISPOSE ALLOC: SAVE RECORD, KILL HEART, DISPOSE TRANSACTION LOCK
	//==================================================================
	private ShutdownDaemonRequest disposeAllocation(int allocId, Exception e) {
		log.trace("Allocation " + allocId + " disposing sequence started...");
		ShutdownDaemonRequest shutdownReq = domain.disposeLiveAllocation(allocId, e);
		super.disposeHeart(allocId);
		return shutdownReq;
	}

	//=============================
	// CALLBACK: HEARTBEAT OVERDUE
	//=============================
	public void processHeartBeatOverdue(Object id) {
		Integer allocId = (Integer)id;
		log.warn("Heartbeat overdue for allocation '" + allocId + "' detected. Disposing allocation " + id);
		disposeAllocation(allocId, null);
	}
}
