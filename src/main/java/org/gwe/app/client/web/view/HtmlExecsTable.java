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

import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.utils.web.HtmlLink;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2008
 */
public class HtmlExecsTable extends HtmlTable {

	public HtmlExecsTable(String clusterId, JobInfo job) {
		if (job == null) return;
	    HtmlTable table = new HtmlTable("Number", "Allocation", "Started", "Prepared", "Dispatched", "Processed", "Completed", "Failed");
        for (JobExecutionInfo exec : job.getExecutions()) {
        	HtmlTableCell num = new HtmlTableCell("[" + exec.getExecutionNum() + " ]", "", new HtmlLink(false, "#" + exec.getExecutionNum()));
    		table.addRow(num, getAllocationId(exec), exec.getWhenCreated(), exec.getWhenPrepared(), 
					exec.getWhenDispatched(), exec.getWhenProcessed(), exec.getWhenCompleted(), exec.getWhenFailed());
        }
    }

	private Object getAllocationId(JobExecutionInfo exec) {
	    return (exec.getAllocation() != null) ? exec.getAllocation().getId() : null;
    }
}
