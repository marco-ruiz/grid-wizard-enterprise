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

package org.gwe.app.client.web.view;

import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderExecutionProfileInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.web.HtmlPropsTable;
import org.gwe.utils.web.WebIcon;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2008
 */
public abstract class HtmlModelPropsTable<MODEL_TYPE> extends HtmlPropsTable {

	protected MODEL_TYPE modelObj;

	public HtmlModelPropsTable(MODEL_TYPE model) {
		this.modelObj = model;
		if (model != null) populate();
	}
	
	protected abstract void populate();
	
	protected void populate(MODEL_TYPE model) {}

	public String getHTML() {
		return isEmpty() ? null : super.getHTML();
	}
}

class HtmlClusterPropsTable extends HtmlModelPropsTable<HeadResourceInfo> {

	public HtmlClusterPropsTable(HeadResourceInfo daemonInfo) { super(daemonInfo); }

	protected void populate() {
        addPropertyRow("Name", modelObj.getName());
        addPropertyRow("Host", modelObj.getHost());
        addPropertyRow("Daemon Installation Path", modelObj.getInstallPath());
        addPropertyRow("Daemon Database Path", modelObj.getDatabasePath());
        addPropertyRow("Queue Size", modelObj.getQueueSize());
        addPropertyRow("Compute Node Wait Connection Timeout", modelObj.getMaxWaitMins() + " minutes");
        addPropertyRow("Compute Node Hijack Timeout", modelObj.getMaxHijackMins() + " minutes");
        addPropertyRow("Compute Node Idle Timeout", modelObj.getMaxIdleMins() + " minutes");
        addPropertyRow("Remote API Servers Registry", modelObj.getLocation());
    }
}

class HtmlOrderPropsTable extends HtmlModelPropsTable<OrderInfo> {

	public HtmlOrderPropsTable(OrderInfo model) { super(model); }

	public void populate() {
	    addPropertyRow("Order Id", modelObj.getId());
        addPropertyRow("Submission Time", modelObj.getWhenCreated());
        addPropertyRow("Completion Time", modelObj.getWhenCompleted());
        addPropertyRow("Jobs Completed", modelObj.getCompletedJobsCount());
        addPropertyRow("Jobs Count", modelObj.getTotalJobsCount());
    }

}

class HtmlOrderProfilePropsTable extends HtmlModelPropsTable<OrderExecutionProfileInfo> {

	public HtmlOrderProfilePropsTable(OrderExecutionProfileInfo model) { super(model); }

	public void populate() {
        addPropertyRow("Jobs launch mode", modelObj.getLaunchModeEnum());
        addMaxTypeProperty("Max retries on failures", modelObj.getMaxRetries());
        addMaxTypeProperty("Max run time per job (seconds)", modelObj.getMaxJobRunningTime());
        addMaxTypeProperty("Max concurrent prepared jobs", modelObj.getMaxPreparedJobs());
        addMaxTypeProperty("Max concurrent running jobs", modelObj.getMaxConcurrentRunningJobs());
	    addPropertyRow("VFS clean up mode", modelObj.getCleanUpModeEnum());
	    addMaxTypeProperty("VFS disk quota", modelObj.getDiskSpaceForVFS());
        addPropertyRow("Use caching for VFS ?", new Boolean(modelObj.isUseVFSCache()).toString().toUpperCase());
    }
	
	private void addMaxTypeProperty(String property, int value) {
		addPropertyRow(property, (value == -1) ? "NO LIMIT" : value + "");
	}
}

class HtmlJobPropsTable extends HtmlModelPropsTable<JobInfo> {

	public HtmlJobPropsTable(JobInfo job) { super(job); }

	protected void populate() {
	    JobExecutionInfo exec = modelObj.getExecution();
        addPropertyRow("Job Id", modelObj.getId());
        addPropertyRow("Order Id", modelObj.getOrderId());
        addPropertyRow("Job Number", modelObj.getJobNum());
        addPropertyRow("Executions", trials(modelObj.getFailures() + 1));
	    addPropertyRow("Status", (exec == null) ? "WAITING" : exec.getStatus());
    }
	
	private StringBuffer trials(int count) {
		StringBuffer result = new StringBuffer(count + " [ ");
		for (int idx = 1; idx < count + 1; idx++)
			result.append(getExecAnchor(idx) + " ");
		return result.append("]");
	}

	private String getExecAnchor(int number) {
	    return "<a href=\"#" + number + "\">" + number + "</a>";
    }
}

class HtmlExecPropsTable extends HtmlModelPropsTable<JobExecutionInfo> {

	public static WebIcon getStatusImage(JobExecutionInfo exec) {
    	if (exec != null) {
	    	if (exec.getWhenFailed() != null) return WebIcon.STATUS_ERROR;
	    	if (exec.getWhenCompleted() != null) return WebIcon.STATUS_OK;
    	}
    	return WebIcon.STATUS_HELP;
	}
	
	public HtmlExecPropsTable(JobExecutionInfo exec) { super(exec); }

	protected void populate() {
	    AllocationInfo alloc = modelObj.getAllocation();
		Integer allocId = (alloc != null) ? alloc.getId() : null;
	    addPropertyRow("Number",          modelObj.getExecutionNum());
		addPropertyRow("Allocation Id",   allocId);
        addPropertyRow("Time Started",    modelObj.getWhenCreated());
        addPropertyRow("Time Prepared",   modelObj.getWhenPrepared());
        addPropertyRow("Time Dispatched", modelObj.getWhenDispatched());
        addPropertyRow("Time Processed",  modelObj.getWhenProcessed());
        addPropertyRow("Time Completed",  modelObj.getWhenCompleted());
        addPropertyRow("Time Failed",     modelObj.getWhenFailed());
    }
}
