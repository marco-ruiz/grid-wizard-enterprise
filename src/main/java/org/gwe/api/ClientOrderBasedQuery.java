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

package org.gwe.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gwe.utils.VelocityUtils;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2007
 */
public enum ClientOrderBasedQuery {

	GET_ORDER("view-order", "Order Id: $!{model.id}\n" +
						"Description: $!{model.description}\n" +
						"Descriptor:\n$!{model.descriptor}\n" +
						"Owner: $!{model.owner.name}\n" +
						"Submitted at: $!{model.whenCreated}\n" +
						"Completed at: $!{model.whenCompleted}\n" + 
						"Progress: $!{model.completedJobsCount}/$!{model.totalJobsCount}\n"), 
	GET_JOB("view-job", "Job Number: $!{model.jobNum}\n" +
					"Descriptor:\n$!{model.descriptor}\n" +
					"Allocation Id: $!{model.execution.allocation.id}\n" +
					"Started at: $!{model.execution.whenCreated}\n" +
					"Prepared at: $!{model.execution.whenPrepared}\n" +
					"Assigned at: $!{model.execution.whenAssigned}\n" + 
					"Dispatched at: $!{model.execution.whenDispatched}\n" +
					"Processed at: $!{model.execution.whenProcessed}\n" +
					"Completed at: $!{model.execution.whenCompleted}\n" +
					"Aborted at: $!{model.execution.whenFailed}\n"), 
//					"Processing Result: $!{job.processResult}"),
	LIST_ORDERS("list-orders", "Id\tSubmitted\t\tCompleted\t\tProgress\n" +
							"#foreach($!{order} in $!{model})" +
							"$!{order.id}\t$!{order.whenCreated}\t$!{order.whenCompleted}\t$!{order.completedJobsCount} / $!{order.totalJobsCount}\n" +
							"#end"), 
	LIST_JOBS("list-jobs", "Num\tFailures\tAllocation\tStarted\t\t\tPrepared\t\tDispatched\t\tProcessed\t\tCompleted\t\tFailed\n" +
						"#foreach($!{job} in $!{model.jobs})" +
						"$!{job.jobNum}\t$!{job.failures}\t\t$!{job.execution.allocation.id}\t$!{job.execution.whenCreated}\t$!{job.execution.whenPrepared}\t$!{job.execution.whenDispatched}\t" +
						"$!{job.execution.whenProcessed}\t$!{job.execution.whenCompleted}\t$!{job.execution.whenFailed}\n" +
						"#end"), 
	STREAM_RESULT("view-result", "$!{model}");
	
	private String id;
	private String template;
	
	ClientOrderBasedQuery(String name, String resultTemplate) { 
		this.id = name;
		this.template = resultTemplate;
	}
	
	public String getOutput(Object model) { 
		return (model != null) ? VelocityUtils.merge("model", model, template) : "No results found.";
	}

	private static Map<String, ClientOrderBasedQuery> operations = new HashMap<String, ClientOrderBasedQuery>();
	static {
		for (ClientOrderBasedQuery oper : ClientOrderBasedQuery.values()) operations.put(oper.id, oper);
	}
	
	public static ClientOrderBasedQuery getQuery(String id) { return operations.get(id); }

	public static Set<String> getQueries() { return operations.keySet(); }
}
