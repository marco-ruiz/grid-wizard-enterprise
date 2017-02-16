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

package org.gwe.persistence.model.order;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.live.JobLive;

/**
 * @author Marco Ruiz
 * @since Aug 18, 2008
 */
public abstract class JobDescriptor implements Serializable {
	
	protected transient JobLive jobLive;
	private transient Map<JobSideWorkerType, JobSideWorker> processors;
	
	public void initExecution(JobLive jobLive) throws Exception {
	    this.jobLive = jobLive;
	    this.processors = initExecutionInternal(jobLive);
    }
	
	protected abstract Map<JobSideWorkerType, JobSideWorker> initExecutionInternal(JobLive jobLive) throws Exception;

	public abstract Map<String, Object> getPermutationValues();
	
    public StringBuffer toCSV(List<String> varNames) {
    	StringBuffer result = new StringBuffer("");
    	Map<String, Object> vars = getPermutationValues();
    	
    	for (String varName : varNames) {
			if (result.length() > 0) result.append(",");
	        Object varValue = vars.get(varName);
			result.append("\"").append(varValue == null ? "" : varValue).append("\"");
        }
	    return result;
    }

	public void preProcess() throws Throwable {
    	processors.get(JobSideWorkerType.PRE).execute(jobLive);
    }

	public void postProcess() throws Throwable {
    	processors.get(JobSideWorkerType.POST).execute(jobLive);
    }

	public void finalizeExecution(JobLive jobLive) {}

	public abstract void processSystemDependencies(DaemonConfigDesc config, JobInfo job);
}

