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

package org.gwe.drivers.resManagers;

import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPI4Agent;
import org.gwe.app.agent.BaseAgent;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Sep 5, 2008
 */
public class MultiThreadsDriver extends ResourceManagerDriver {

	private static Log log = LogFactory.getLog(MultiThreadsDriver.class);

	private ExecutorService agentThreads = ThreadPoolUtils.createThreadPool("Local GWE Agents");
	private ServerAPI4Agent agentAPI;

	public boolean isSupportedJobManagerAvailable() { return true; }

/*
    protected String requestAgentLaunch(AllocationInfo alloc) {
		BaseAgent agent = new BaseAgent(agentAPI, alloc.getId(), alloc.getWorkspacePath());
		agentThreads.submit(agent);
		return alloc.getId() + "";
    }
*/

	public ServerAPI4Agent getAgentAPI() {
    	return agentAPI;
    }

	public void setAgentAPI(ServerAPI4Agent agentAPI) {
    	this.agentAPI = agentAPI;
    }

	public AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return null;
    }

    public AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return null;
    }

	@Override
    public String queueAllocationRequest(AllocationInfo alloc) throws ResourceAllocationException {
		agentThreads.submit(new BaseAgent(agentAPI, configuration, alloc.getId()));
	    return alloc.getId() + "";
    }
}

