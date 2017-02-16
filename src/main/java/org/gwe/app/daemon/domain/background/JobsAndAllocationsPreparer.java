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

package org.gwe.app.daemon.domain.background;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.daemon.domain.AgentDomain;
import org.gwe.drivers.resManagers.GridResourceManager;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Nov 19, 2007
 */
public class JobsAndAllocationsPreparer extends AgentDomainBasedService {

	private static Log log = LogFactory.getLog(JobsAndAllocationsPreparer.class);
	
	private ExecutorService allocCreatorsThreadPool = ThreadPoolUtils.createThreadPool("Allocation Schedulers");
	private GridResourceManager gridResourceManager = null;

	public JobsAndAllocationsPreparer(AgentDomain agentDom, GridResourceManager gridResourceManager) {
		super("Jobs & Allocations Preparer", agentDom);
		this.gridResourceManager = gridResourceManager;
	}

	public void execute() throws Exception {
		final List<AllocationInfo> allocations = agentDom.prepareMoreAllocations();
		// IMPORTANT: If the preparation cycle is too quick the agent may have time to come back and interfere with these JobInfo updates 
		List<JobInfo> jobs = agentDom.selectNextJobBatch();
		// IMPORTANT: If the selection cycle is too slow the preparers may finished before the jobs get updated into the DB causing sync issues 
		agentDom.launchPreparers(jobs);

		log.info("Allocating compute resources: " + allocations);
		for (AllocationInfo alloc : allocations) allocComputeResourceAsync(alloc);
	}

	private void allocComputeResourceAsync(final AllocationInfo alloc) {
	    allocCreatorsThreadPool.submit(new Runnable() {
	    	public void run() {
    			// For each allocation queue a compute resource allocation requests in local resource manager 
	    		try {
                	createTooLateTimer(alloc);
                	gridResourceManager.getResourceManagerDriver().allocateComputeResource(alloc);
                } catch (Exception e) {
                	log.warn("Exception thrown while trying to allocate a compute resource.", e);
                }
	    	}
	    });
    }

	private void createTooLateTimer(final AllocationInfo alloc) {
		long maxAttachMillis = agentDom.getConfig().getHeadResource().getMaxAttachMillis();
		if (maxAttachMillis == 0 || maxAttachMillis == -1) return;
		
		TimerTask releaseTooLateTask = new TimerTask() {
			public void run() {
				alloc.flagAsTooLate();
				agentDom.triggerMoreJobsAndAllocationPreparation();
			}
		};
			
		alloc.setTooLateTimerTask(releaseTooLateTask, maxAttachMillis);
	}
}
