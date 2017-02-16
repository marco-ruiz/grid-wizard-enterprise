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

package org.gwe.app.daemon.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.live.LiveAllocations;
import org.gwe.persistence.model.live.LiveExecutions;
import org.gwe.persistence.model.live.LiveOrders;
import org.gwe.persistence.model.live.LiveOrdersProgress;
import org.gwe.persistence.model.live.OrderIncrementalProgress;
import org.gwe.persistence.model.live.OrderLive;
import org.gwe.utils.concurrent.BooleanLock;
import org.gwe.utils.concurrent.ThreadPoolUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Marco Ruiz
 * @since Jan 1, 2009
 */
public class InternalDomain extends BaseDomain {

	private static Log log = LogFactory.getLog(InternalDomain.class);

	private static final int JOB_PREPARATION_OVER_ALLOCATIONS_FACTOR = 1;
	
	private ExecutorService allocKillersThreadPool  = ThreadPoolUtils.createThreadPool("Allocation Killers");
	private ExecutorService jobExecutionFinalizersThreadPool = ThreadPoolUtils.createThreadPool("Jobs Finalizers");

	private LiveOrders liveOrders = new LiveOrders();
	private LiveExecutions liveExecutions = new LiveExecutions();
	private LiveAllocations liveAllocs = new LiveAllocations();
	private LiveOrdersProgress ordersProgress = new LiveOrdersProgress();
	
	private BooleanLock preparerLock = new BooleanLock();

	//================
	// Jobs Generator
	//================
	public void triggerMoreJobsAndAllocationPreparation() {
		preparerLock.releaseLock();
	}

	//==============================
	// Jobs and Allocation Preparer
	//==============================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<AllocationInfo> prepareMoreAllocations() {
		preparerLock.waitUntilReleased();
		int pending = (int)jobDAO.getPendingJobsCount() + liveExecutions.size();
		int amount = liveAllocs.calculateDeficit(pending, getConfig().getHeadResource().getQueueSize());

		// Create allocations and submit allocation requests to local resource manager 
        List<AllocationInfo> allocations = new ArrayList<AllocationInfo>();
        if (amount > 0) {  
        	// Create allocation pojos and persist them
        	for (int count = 0; count < amount; count++) allocations.add(new AllocationInfo());
        	allocationDAO.saveAll(allocations);
        	liveAllocs.saveLiveAllocations(allocations);
        }
        return allocations;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<JobInfo> selectNextJobBatch() {
		List<JobInfo> jobs = new ArrayList<JobInfo>(); 
		int amount = liveAllocs.getLiveAllocationsCount() * JOB_PREPARATION_OVER_ALLOCATIONS_FACTOR - liveExecutions.size();
		
		// Load next batch of jobs to execute
		if (amount > 0) {
			jobs = jobDAO.getNextSchedulableJobsBatch(amount);
			for (JobInfo job : jobs) {
	            JobExecutionInfo exec = new JobExecutionInfo(job);
	            jobExecutionDAO.save(exec);
            }
			jobDAO.saveOrUpdateAll(jobs);
		}
		
		// Load corresponding orders 
		Set<Integer> orderIds = liveOrders.getJobsOrdersNotStarted(jobs);
        if (!orderIds.isEmpty()) {
        	List<OrderInfo> orders = orderDAO.getForFieldIn("id", orderIds);
        	liveOrders.add(orders);
        	for (OrderInfo order : orders) orderDAO.evict(order);
        }

        return jobs;
	}

	@Transactional(propagation=Propagation.NEVER)
	public void launchPreparers(List<JobInfo> jobs) {
	    // Create live executions and launch them async
        Map<JobInfo, OrderLive> nextLiveExecs = liveOrders.getJobLiveParams(jobs);
		liveExecutions.prepareExecutions(nextLiveExecs);
    }

	//========================
	// Jobs Readiness Flagger
	//========================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void flagNextPreparedJobAsReady() throws InterruptedException, ExecutionException {
		JobExecutionInfo exec = liveExecutions.transitionNextFinishedPreparingToPrepared();
		if (exec.hasFailed()) 
			wrapUpJob(exec);
		else 
			jobExecutionDAO.update(exec);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void wrapUpJob(JobExecutionInfo exec) {
		try {
			JobInfo job = exec.getJob();
			if (job.computeFinishedExecutionStatus()) {
				ordersProgress.incrementJobsFinished(job.getOrderId());
				log.info("Execution " + exec.getId() + " finished associated job. Success: " + !exec.hasFailed() + ". Job permanent execution: " + job.getExecution());
			}

			jobExecutionDAO.update(exec);
			jobDAO.updateJob(job);
	        log.info("Job " + job.getId() + " updated after an associated execution finished");
	
	        triggerMoreJobsAndAllocationPreparation();
		} catch (Exception e) {
			log.warn("Problems wrapping up job execution " + exec.getId(), e);
		}
	}

	//============================
	// Job and Allocation Matcher
	//============================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void pairNextJobAndAllocationReady() throws Exception {
		triggerMoreJobsAndAllocationPreparation();
		JobExecutionInfo exec = liveExecutions.getNextPreparedExecution();
		liveAllocs.pairNextReadyAllocationWithJob(exec);
		jobExecutionDAO.update(exec);
	}

	//===================
	// Orders DB Updater
	//===================
	public void runUpdateOrdersCycle() {
		OrderIncrementalProgress progress = ordersProgress.extractNextIncrementalProgress();
		updateOrder(progress.getOrderId(), progress.getJobsCompletedIncrement());
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void updateOrder(int orderId, int amount) {
		OrderInfo order = orderDAO.get(orderId);
		order.incrementJobsCompleted(amount);
		if (order.isFinished()) liveOrders.dispose(order.getId());
		orderDAO.update(order);
	}
}
