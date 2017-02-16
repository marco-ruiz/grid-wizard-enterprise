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

package org.gwe.app.agent;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPI4Agent;
import org.gwe.api.SystemDaemonRequest;
import org.gwe.persistence.model.ComputeResourceInfo;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.order.DaemonRequest;
import org.gwe.utils.reinvoke.ReinvocationInterceptor;

/**
 * @author Marco Ruiz
 * @since Sep 5, 2008
 */
public class BaseAgent implements Callable<Void> {

	private static Log log = LogFactory.getLog(BaseAgent.class);

	protected ServerAPI4Agent agentAPI;
	public int allocId;
	private String workspace;
	public DaemonConfigDesc config;

	public BaseAgent(ServerAPI4Agent agentAPI, DaemonConfigDesc user, int allocId) {
	    this.agentAPI = ReinvocationInterceptor.createProxy(agentAPI);
	    this.allocId = allocId;
	    this.config = user;
		this.workspace = config.getHeadResource().getInstallation().getAllocsWorkspacePath(allocId);
    }
	
	public Void call() throws Exception {
		boolean accepted = agentAPI.reserveAllocation(allocId, ComputeResourceInfo.createLocalInfo(allocId));
		log.info("Agent registered: '" + accepted + "'");
		while (accepted && processRequest());
		log.info("Shuting down...");
		return null;
	}

	private boolean processRequest() throws RemoteException {
		Serializable result = null;
		DaemonRequest<?> req = null;
		try {
			log.info("Querying daemon for next request...");
			req = queryNextRequest();
		} catch(Exception e) {
			// TODO: Try to recover!!!
			log.warn("Request process failed with exception", e);
			agentAPI.reportAgentProblem(allocId, e);
			return false;
		}
		
		if (req == null) {
			log.info("Maximum retransmissions reached while trying to contact daemon...");
			return false;
		}
		
		if (req instanceof SystemDaemonRequest) { 
			log.info("Daemon sent a " + req + " request...");
			return ((SystemDaemonRequest)req).systemProcess();
		}

		log.info("Invoking 'request.process'...");
		try {
			result = req.process(workspace);
			log.info("Request process completed successfully!");
		} catch(Exception e) {
			// TODO: Try to recover!!!
			result = e;
			log.warn("Request process failed with exception", e);
		}

	    return agentAPI.reportRequestCompletion(allocId, req.getExecId(), result);
	}

	private DaemonRequest<?> queryNextRequest() throws RemoteException {
	    try {
	    	return agentAPI.getNextRequest(allocId);
	    } catch (RemoteException e) {
			log.info("Exception while querying next request...", e);
    		return agentAPI.getNextRequestAgain(allocId);
	    }
    }
}

