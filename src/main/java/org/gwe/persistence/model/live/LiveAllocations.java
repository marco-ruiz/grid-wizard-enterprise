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

package org.gwe.persistence.model.live;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.utils.concurrent.BlockingMap;

/**
 * 
 * @author Marco Ruiz
 * @since Nov 15, 2007
 */
public class LiveAllocations {

	private Map<Integer, AllocationInfo> busyAllocations = new HashMap<Integer, AllocationInfo>();
	private BlockingMap<Integer, AllocationInfo> readyAllocations = new BlockingMap<Integer, AllocationInfo>();
	
	public void saveLiveAllocations(List<AllocationInfo> allocList) {
		synchronized (readyAllocations) {
			for (AllocationInfo alloc : allocList) busyAllocations.put(alloc.getId(), alloc);
		}
	}

	public void saveLiveAllocation(AllocationInfo alloc) {
		synchronized (readyAllocations) {
			busyAllocations.put(alloc.getId(), alloc);
		}
	}

	public AllocationInfo getBusyAllocation(int allocId) throws AllocationNotFoundException {
		synchronized (readyAllocations) {
			AllocationInfo alloc = busyAllocations.get(allocId);
			if (alloc == null) throw new AllocationNotFoundException(allocId); 
			return alloc;
		}
	}
	
	public AllocationInfo getAllocation(int allocId) throws AllocationNotFoundException {
		AllocationInfo alloc = null;
		synchronized (readyAllocations) {
			alloc = busyAllocations.get(allocId);
			if (alloc == null) alloc = readyAllocations.get(allocId);
			if (alloc == null) throw new AllocationNotFoundException(allocId);
		}
		// Just wait for this ready allocation to get assigned a job execution
		alloc.getNextProcessingExecutionBlocking();    
		return alloc;
	}
	
	public AllocationInfo removeAllocation(int allocId) {
		synchronized (readyAllocations) {
			AllocationInfo busyAlloc = busyAllocations.remove(allocId);
			return (busyAlloc != null) ? busyAlloc : readyAllocations.remove(allocId); 
		}
	}

	public int getLiveAllocationsCount() {
		synchronized (readyAllocations) {
			return busyAllocations.size() + readyAllocations.size();
		}
	}

	public int calculateDeficit(int jobsPending, int maxLiveAllocs) {
	    int result = jobsPending;
		if (maxLiveAllocs > 0) {
			int unusedQueueSize = maxLiveAllocs - getLiveAllocationsCount();
			if (jobsPending > unusedQueueSize) result = unusedQueueSize;
		} else {
			result -= getLiveAllocationsCount();
		}
	    return result;
    }

	public void flagAsReady(AllocationInfo alloc) {
		synchronized (readyAllocations) {
			Integer allocId = alloc.getId();
			readyAllocations.put(allocId, busyAllocations.remove(allocId));
			alloc.getDeathDealer().startIdleCountdown();
		}
	}

	public void pairNextReadyAllocationWithJob(JobExecutionInfo exec) {
		synchronized (readyAllocations) {
			AllocationInfo alloc;
			while (true) {
				alloc = readyAllocations.takeOne();
				assert alloc != null;
				if (alloc.setProcessingExecution(exec)) break;
			}
			busyAllocations.put(alloc.getId(), alloc);
		}
	}
}
