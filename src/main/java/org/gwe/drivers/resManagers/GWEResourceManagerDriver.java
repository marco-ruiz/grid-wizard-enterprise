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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.AllocationInfo;

/**
 * @author Marco Ruiz
 * @since Mar 26, 2008
 */
public class GWEResourceManagerDriver extends ResourceManagerDriver {

	private static Log log = LogFactory.getLog(GWEResourceManagerDriver.class);
	
	private Map<String, Integer> computeNodesAddressesAndProcessors = new HashMap<String, Integer>();
	
	public void setComputeNodes(Map<String, Integer> addresses) {
		computeNodesAddressesAndProcessors.putAll(addresses);
	}
	
	public boolean isSupportedJobManagerAvailable() {
		return !computeNodesAddressesAndProcessors.isEmpty();
	}

	public String queueAllocationRequest(AllocationInfo alloc) throws ResourceAllocationException {
		String[] results = runQueueCommand(alloc.getAgentScriptFileName(), alloc);
		return null;
    }

    public AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return checkAllocation(alloc);
    }

    public AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return AllocationPhase.RELEASED;
    }
}
