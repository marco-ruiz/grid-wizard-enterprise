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

package org.gwe.persistence.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.live.JobLive;
import org.gwe.persistence.model.live.OrderLive;
import org.gwe.persistence.model.order.DaemonRequest;
import org.gwe.persistence.model.order.JobDescriptor;
import org.gwe.utils.IOUtils;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author Marco Ruiz
 * @since Aug 8, 2007
 */
@GenericGenerator(name = "jobInfoIdGenerator", strategy = "org.gwe.persistence.model.JobInfoIdGenerator")
@Entity
public class JobInfo extends BaseModelInfo<String> {
	
	private static Log log = LogFactory.getLog(JobInfo.class);

	private static final String JOB_WORKSPACE_PATH_PREFIX = "job-";

	// Creation Time
	@Id
	@GeneratedValue(generator = "jobInfoIdGenerator")
	private String id;

	@ManyToOne(fetch = FetchType.EAGER)
	private OrderInfo order;
	private int jobNum;

    @Lob @Column(length = 20480)
    private JobDescriptor descriptor; 
    
	@Lob @Column(length = 32768)
	protected DaemonRequest<?> request;
	
	@Column
	private int failures = 0;

	@OneToOne(fetch = FetchType.EAGER)
	private JobExecutionInfo execution = null;
	
	@Transient
	private List<JobExecutionInfo> executions = new ArrayList<JobExecutionInfo>();
	
	public JobInfo() {}

	public JobInfo(int jobNum, JobDescriptor descriptor, DaemonRequest<?> daemonRequest) {
		this.jobNum = jobNum;
    	this.descriptor = descriptor;
		this.request = daemonRequest;
	}

	// Creation Time
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public OrderInfo getOrder() {
		return order;
	}

	public void setOrder(OrderInfo order) {
		this.order = order;
	}

	public int getJobNum() {
		return jobNum;
	}
	
	public JobDescriptor getDescriptor() {
    	return descriptor;
    }

	public int getFailures() {
    	return failures;
    }

	public JobExecutionInfo getExecution() {
    	return execution;
    }

	public void setExecution(JobExecutionInfo execution) {
    	this.execution = execution;
    }
	
	public List<JobExecutionInfo> getExecutions() {
    	return executions;
    }

	public void setExecutions(List<JobExecutionInfo> executions) {
    	this.executions = executions;
    }

	public boolean computeFinishedExecutionStatus() {
		boolean success = !execution.hasFailed();
		boolean finished = success || failures >= order.getExecutionProfile().getMaxRetries();
		if (!success && !finished) {
			execution = null;
			failures++;
		}
		return finished;
	}

/*
	public long getComputeTime() {
		long endTime = 0;
		if (whenProcessed != null && whenFailed == null) endTime = whenProcessed.getTime();  
		if (whenProcessed == null && whenFailed != null) endTime = whenFailed.getTime();  
		return (endTime == 0) ? 0 : endTime - whenStarted.getTime(); 
	}
*/

	public DaemonRequest<?> getRequest() {
		return request;
	}

	public String getWorkspaceInDaemon(DaemonConfigDesc config) {
		return IOUtils.concatenatePaths(order.getWorkspaceInDaemon(config), JOB_WORKSPACE_PATH_PREFIX + getId());
	}
	
	public void preProcess(OrderLive orderLive) {
		JobLive jobLive = new JobLive(this, orderLive);
		try {
			request.setExecId(execution.getId());
			request.setKeys(orderLive.getConfig().getKeys());
			descriptor.initExecution(jobLive);
			descriptor.preProcess();
			logExecutionPhase(EventType.JOB_PREPARED, null);
		} catch (Throwable e) {
			logFailure(e);
			descriptor.finalizeExecution(jobLive);
		} 
	}

	public void postProcess(OrderLive orderLive, Serializable result) {
		JobLive jobLive = new JobLive(this, orderLive);
		try {
			if (result instanceof Exception) {
				descriptor.postProcess();
				logFailure((Exception) result);
			} else {
				logExecutionPhase(EventType.JOB_PROCESSED, result);
				descriptor.postProcess();
				Serializable parsedResult = order.createResultParser().parseResult(execution.getRequestResult());
				logExecutionPhase(EventType.JOB_COMPLETED, parsedResult);
			}
		} catch (Throwable e) {
			logFailure(e);
		} finally {
			descriptor.finalizeExecution(jobLive);
		}
	}

	private void logFailure(Throwable e) {
	    execution.logFailure(e);
    }

	private void logExecutionPhase(EventType evType, Serializable result) {
	    execution.logExecutionPhase(evType, result);
    }

	public void logCreateEvent() {
		logEvent(EventType.CREATED, getWhenCreated(), getOrder());
	}

	public ModelSummary<String> createModelSummaryFor(EventType ev) {
		switch (ev) {
			case JOB_ASSIGNED:
//				return new ModelSummary<String>(this, allocation.getId());
				return new ModelSummary<String>(this, execution);
	
			default:
				return super.createModelSummaryFor(ev);
		}
	}
	
	public String toString() {
		return "jobId:" + getId();
	}
	
	// TODO: Revisit this pseudo hack
	public int getOrderId() {
		return Integer.parseInt(InfoUtils.getIdPieces(this)[0]); 
	}
}
