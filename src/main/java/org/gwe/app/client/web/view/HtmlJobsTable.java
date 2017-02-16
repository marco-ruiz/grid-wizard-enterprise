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

import org.gwe.app.client.web.request.Param;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.web.HtmlLink;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2008
 */
public class HtmlJobsTable extends HtmlTable {

	public HtmlJobsTable(String clusterId, OrderInfo order) {
	    super("Status", "Number", "Failures", "Allocation", "Started", "Prepared", "Dispatched", "Processed", "Completed", "Failed");
        for (JobInfo job : order.getJobs()) {
        	HtmlTableCell num = new HtmlTableCell("[ " + job.getJobNum() + " ]", "", new HtmlJobLink(clusterId, job)) ;
        	JobExecutionInfo exec = job.getExecution();
        	HtmlTableCell status = new HtmlTableCell("", "", null, HtmlExecPropsTable.getStatusImage(exec));
        	if (exec != null) {
        		Object allocId = (exec.getAllocation() != null) ? exec.getAllocation().getId() : null;
				addRow(status, num, job.getFailures(), allocId, exec.getWhenCreated(), exec.getWhenPrepared(), 
						exec.getWhenDispatched(), exec.getWhenProcessed(), exec.getWhenCompleted(), exec.getWhenFailed());
        	} else {
        		addRow(status, num, null, null, null, null, null, null, null, null);
        	}
        }
    }
}

class HtmlJobLink extends HtmlLink {

	public HtmlJobLink(String clusterId, JobInfo job) {
	    super(false, "job");
	    addParam(Param.CLUSTER_ID, clusterId);
	    addParam(Param.ORDER_ID, job.getOrder().getId());
	    addParam(Param.JOB_NUM, job.getJobNum());
    }
}
