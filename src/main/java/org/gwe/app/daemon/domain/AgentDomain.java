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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ShutdownDaemonRequest;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.ComputeResourceInfo;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.live.AllocationNotFoundException;
import org.gwe.persistence.model.live.LiveAllocations;
import org.gwe.persistence.model.live.LiveExecutions;
import org.gwe.persistence.model.live.LiveOrders;
import org.gwe.persistence.model.live.LiveOrdersProgress;
import org.gwe.persistence.model.live.OrderIncrementalProgress;
import org.gwe.persistence.model.live.OrderLive;
import org.gwe.persistence.model.order.DaemonRequest;
import org.gwe.utils.concurrent.BooleanLock;
import org.gwe.utils.concurrent.ThreadPoolUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Marco Ruiz
 * @since Sep 19, 2007
 */
public class AgentDomain extends BaseDomain {
	
	private static Log log = LogFactory.getLog(AgentDomain.class);

//	public static final int DEFAULT_HEART_BEAT_PERIOD = 60000; // 1 minute
	public static final int JOB_PREPARATION_OVER_ALLOCATIONS_FACTOR = 1;
	
	private ExecutorService allocKillersThreadPool  = ThreadPoolUtils.createThreadPool("Allocation Killers");
	private ExecutorService jobExecutionFinalizersThreadPool = ThreadPoolUtils.createThreadPool("Jobs Finalizers");

	private LiveOrders liveOrders = new LiveOrders();
	private LiveExecutions liveExecutions = new LiveExecutions();
	private LiveAllocations liveAllocs = new LiveAllocations();
	private LiveOrdersProgress ordersProgress = new LiveOrdersProgress();
	
	private BooleanLock preparerLock = new BooleanLock();

	public void setConfig(DaemonConfigDesc config) {
		super.setConfig(config);
		liveOrders.setConfig(config);
    }

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public boolean attachAllocation(int allocId, ComputeResourceInfo compRes) throws AllocationNotFoundException, AllocationTooLateException {
		AllocationInfo alloc = liveAllocs.getBusyAllocation(allocId);
		if (alloc.isRegistered() && !compRes.equals(alloc.getCompResource())) return false;
		if (alloc.isTooLate())
			throw new AllocationTooLateException();
			
		if (compRes != null) {
			alloc.setCompResource(compRes);
			allocKillersThreadPool.submit(alloc.createDeathDealer(getConfig()));
//			computeResourceDAO.saveOrUpdate(compRes);
		}
		allocationDAO.update(alloc);
		return true;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public DaemonRequest<?> extractRequestFromNextJobAssignedBlocking(int allocId) throws AllocationNotFoundException, NoJobToProcessException {
		AllocationInfo alloc = liveAllocs.getBusyAllocation(allocId);
		liveAllocs.flagAsReady(alloc);
		// Will wait until a job is assigned
		JobExecutionInfo exec = alloc.getNextProcessingExecutionBlocking();
		if (exec == null) throw new NoJobToProcessException();
		exec.flagAsDispatched();
		jobExecutionDAO.update(exec);
		return exec.getJob().getRequest();
	}

	public DaemonRequest<?> getCurrentProcessingExecution(int allocId) throws AllocationNotFoundException, NoJobToProcessException {
		AllocationInfo alloc = liveAllocs.getAllocation(allocId);
		JobExecutionInfo exec = alloc.getProcessingExecution();
		if (exec == null) throw new NoJobToProcessException();
		return exec.getJob().getRequest();
	}

	public void cleanAllocAndFinalizeJobAsync(int allocId, String execId, final Serializable result) throws AllocationNotFoundException {
		final AllocationInfo alloc = liveAllocs.getBusyAllocation(allocId);
		final JobExecutionInfo execProcessed = alloc.getProcessingExecution();
		if (execProcessed == null) {
			log.warn("Job processed lost from allocation " + alloc.getId() + " with results:\n" + result);
			return;
		}
		if (!execProcessed.getId().equals(execId)) {
			log.warn("Ignoring result sent by allocation " + allocId + " for execution " + execId + " (expected: " + execProcessed.getId() + ")");
			return;
		}
		alloc.extractProcessingExecution();
		
		jobExecutionFinalizersThreadPool.submit(new Runnable() {
			public void run() { 
				OrderLive orderLive = liveOrders.get(execProcessed.getJob().getOrderId());
				execProcessed.getJob().postProcess(orderLive, result);
				wrapUpJob(execProcessed); 
			}
		});
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ShutdownDaemonRequest disposeLiveAllocation(int allocId, Exception ex) {
		if (ex != null && !(ex instanceof NoJobToProcessException)) 
			log.error("Error encountered while executing a domain services. Disposing allocation " + allocId, ex);

		int releaseReason = -1;
		AllocationInfo alloc = liveAllocs.removeAllocation(allocId);
		if (alloc != null) {
			JobExecutionInfo orphanExecution = alloc.dispose(ex == null);
			String orphanExecId = "[NO ORPHAN EXECUTION]";
			if (orphanExecution != null) {
				liveExecutions.addPreparedExecution(orphanExecution);  // Reschedule job
				orphanExecId = orphanExecution.getId();
			}
			allocationDAO.update(alloc);
			releaseReason = alloc.getReleaseReason();
			log.warn("Allocation '" + allocId + "' disposed and its now orphan execution '"+ orphanExecId + "' rescheduled!");
		} else {
			log.warn("Allocation '" + allocId + "' not found. Most likely it has already been disposed!");
		}
		
		triggerMoreJobsAndAllocationPreparation();
		getConfig().getHeadResource().getInstallation().disposeAllocFolder(allocId);
		return new ShutdownDaemonRequest(releaseReason, ex);
	}

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

