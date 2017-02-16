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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.utils.concurrent.BlockingList;
import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Nov 27, 2007
 */
public class LiveExecutions {

	private CompletionService<JobExecutionInfo> execPreparersThreadPool = 
		new ExecutorCompletionService<JobExecutionInfo>(ThreadPoolUtils.createThreadPool("Jobs Preparers"));

	private BlockingList<JobExecutionInfo> preparedExecs = new BlockingList<JobExecutionInfo>();
	private List<JobExecutionInfo> preparingExecs = new ArrayList<JobExecutionInfo>();
	private DaemonConfigDesc config;
	
	public void setConfig(DaemonConfigDesc config) {
		this.config = config;
    }

	public void prepareExecutions(Map<JobInfo, OrderLive> jobs) {
		Map<JobExecutionInfo, OrderLive> executions = new HashMap<JobExecutionInfo, OrderLive>();
		for (Map.Entry<JobInfo, OrderLive> entry : jobs.entrySet())
	        executions.put(entry.getKey().getExecution(), entry.getValue());
		
		synchronized (preparedExecs) {
			preparingExecs.addAll(executions.keySet());
		}
		for (Map.Entry<JobExecutionInfo, OrderLive> entry : executions.entrySet()) {
			final JobExecutionInfo execution = entry.getKey();
			final OrderLive orderLive = entry.getValue();
			Runnable preparer = new Runnable() {
            	public void run() {
            		execution.getJob().preProcess(orderLive);
            	}
            };
			execPreparersThreadPool.submit(preparer, execution);
		}
	}
	
	public JobExecutionInfo transitionNextFinishedPreparingToPrepared() throws InterruptedException, ExecutionException {
		JobExecutionInfo exec = execPreparersThreadPool.take().get();
		synchronized (preparedExecs) {
			preparingExecs.remove(exec);
			if (!exec.hasFailed()) addPreparedExecution(exec);
			return exec;
		}
	}

	public void addPreparedExecution(JobExecutionInfo exec) {
		if (exec != null) preparedExecs.add(exec);
	}

	public JobExecutionInfo getNextPreparedExecution() {
		return preparedExecs.takeOne();
	}
	
	public int size() {
		synchronized (preparedExecs) {
			return preparingExecs.size() + preparedExecs.size();
		}
	}
}

