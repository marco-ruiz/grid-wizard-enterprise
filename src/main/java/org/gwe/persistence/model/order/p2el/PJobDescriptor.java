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

package org.gwe.persistence.model.order.p2el;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.fileSystems.staging.FilesStager;
import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.P2ELFunctionNotSupported;
import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.PProcessorType;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.JobInfoIdGenerator;
import org.gwe.persistence.model.live.JobLive;
import org.gwe.persistence.model.order.JobDescriptor;
import org.gwe.persistence.model.order.JobSideWorker;
import org.gwe.persistence.model.order.JobSideWorkerType;
import org.gwe.persistence.model.order.OSCommandDaemonRequest;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Aug 8, 2007
 */
public class PJobDescriptor extends JobDescriptor {
	
	private static Log log = LogFactory.getLog(PJobDescriptor.class);

	private static final String VAR_USER_HOME = "USER_HOME";
	private static final String VAR_ORDER_ID  = "ORDER_ID";
	private static final String VAR_JOB_NUM   = "JOB_NUM";
	private static final String VAR_JOB_ID    = "JOB_ID";
	
	private PPermutation permutation;
	private PPermutation compiledPermutation;
	private String template;
	
    public PJobDescriptor() {}
	
    public PJobDescriptor(PPermutation permutation, String template) {
		this.permutation = permutation;
		this.template = template;
	}
	
    public void processSystemDependencies(DaemonConfigDesc config, JobInfo job) {
	    PStatementContext ctx = new PStatementContext(null, config.getKeys(), null);
	    ctx.addSystemVar(VAR_ORDER_ID, job.getOrder().getId() + "");
	    ctx.addSystemVar(VAR_JOB_NUM, job.getJobNum() + "");
	    ctx.addSystemVar(VAR_JOB_ID, new JobInfoIdGenerator().generateId(job));
    	try {
	        permutation.processSystemDependencies(ctx);
        } catch (Exception e) {
        	// TODO: Handle better!
        	log.warn("Problems processing variables with system value dependencies", e);
        }
    }
    
    protected Map<JobSideWorkerType, JobSideWorker> initExecutionInternal(JobLive jobLive) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
		IOUtils.createLocalFolder(jobLive.getWorkspacePath());

        compiledPermutation = permutation.clone(); 
        compiledPermutation.processRuntimedependencies(createP2ELContext(jobLive));
		final String command = compiledPermutation.merge(template);
		
		Map<JobSideWorkerType, JobSideWorker> result = new HashMap<JobSideWorkerType, JobSideWorker>();
		
		result.put(JobSideWorkerType.PRE, new PJobSideWorker(compiledPermutation, PProcessorType.PRE) {
            protected void beforeExecuteProcessors(JobLive jobLive) throws Exception {
        		OSCommandDaemonRequest<?> osRequest = (OSCommandDaemonRequest<?>)jobLive.getInfo().getRequest();
        		osRequest.setWorkspacePath(jobLive.getWorkspacePath());
				osRequest.setOSCommand(command);
            }
		});
		
		result.put(JobSideWorkerType.POST, new PJobSideWorker(compiledPermutation, PProcessorType.POST) {
            protected void beforeExecuteProcessors(JobLive jobLive) throws Exception {
        		JobInfo jobInfo = jobLive.getInfo();
				Serializable requestResult = jobInfo.getExecution().getRequestResult();
        		if (requestResult instanceof String)
        			requestResult = (requestResult != null) ? IOUtils.readFile(new File((String)requestResult)) : "";
        		jobInfo.getExecution().setRequestResult(requestResult);
            }
		});
		
		return result;
    }
    
    private PStatementContext createP2ELContext(JobLive jobLive) {
	    PStatementContext result = new PStatementContext(jobLive.getWorkspacePath(), jobLive.getOrderLive().getConfig().getKeys(), getFileStager(jobLive));
    	result.addRuntimeVar(VAR_USER_HOME, jobLive.getOrderLive().getUserHomePath());
		return result;
    }

    public void finalizeExecution(JobLive jobLive) {
		if (jobLive.canCleanUp())
			getFileStager(jobLive).cleanUp(jobLive.getWorkspacePath());
    }

	private FilesStager getFileStager(JobLive jobLive) {
	    POrderDescriptor orderDesc = jobLive.getOrderDescriptor();
	    return orderDesc.getFileStager();
    }

	public Map<String, Object> getPermutationValues() {
		return getPermutation().asFriendlyTreeMap();
	}

	public String toStringDetailed() {
		StringBuffer result = new StringBuffer("TEMPLATE: " + template + "\nPERMUTATION VALUES: \n");
		for (String entryStr : getPermutation().getEntriesStr()) {
	        result.append("\t");
			result.append(entryStr);
			result.append("\n");
		}
		return result.toString();
	}
	
	public String toString() {
		return getPermutation().merge(template);
	}
	
	public String getTemplate() {
    	return template;
    }

	public PPermutation getPermutation() {
	    return (compiledPermutation == null) ? permutation : compiledPermutation;
    }
	
	public List<String> getUploads() {
		List<String> result = new ArrayList<String>();
		if (compiledPermutation != null) {
			Map<String, Object> values = compiledPermutation.asFriendlyTreeMap();
			for (PVariable var : compiledPermutation.keySet()) {
	            if (var.getFunctionInvocation().getFunctionName().equals("out"))
	            	result.add(values.get(var.getFullName()).toString());
            }
		}
		return result;
	}
}

