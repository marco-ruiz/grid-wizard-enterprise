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

package org.gwe.drivers.resManagers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.utils.IOUtils;
import org.gwe.utils.VelocityUtils;

/**
 * @author Marco Ruiz
 * @since Oct 26, 2008
 */
public abstract class CommandLineResourceManagerDriver extends ResourceManagerDriver {

	private static Log log = LogFactory.getLog(CommandLineResourceManagerDriver.class);

	protected String discoveryCommand;
	protected String submissionCommand;
	protected String template;
	
	public CommandLineResourceManagerDriver(String discoveryCommand, String submissionCommand, String submissionTemplateFile) {
		this.discoveryCommand = discoveryCommand;
		this.submissionCommand = submissionCommand;
		this.template = IOUtils.readClassPathFile(submissionTemplateFile);
	}

	public boolean isSupportedJobManagerAvailable() {
		try {
			log.info("Opening a local shell connection to discover if resource manager associated with driver " + this.getClass() + " is installed...");
			new ShellCommand(discoveryCommand).runLocally();
			return true;
		} catch (Exception e) {
			log.info("Driver " + this.getClass() + " could not detect an associated resource manager installed! - " + e.getMessage());
			return false;
		}
	}
	
	public String queueAllocationRequest(AllocationInfo alloc) throws ResourceAllocationException {
		writeAllocationFile(alloc.getAgentScriptFileName(), agentScriptPreffix + alloc.getId(), alloc);
		String[] results = runQueueCommand(submissionCommand + " " + writeSubmitFile(alloc), alloc);
		return extractIdFromSubmissionOutput(results);
    }
	
    protected abstract String extractIdFromSubmissionOutput(String[] results);

    protected String writeSubmitFile(AllocationInfo alloc) throws ResourceAllocationException {
	    writeAllocationFile(alloc.getAgentSubmitFileName(), getSubmitContent(alloc), alloc);
	    return IOUtils.concatenatePaths(getAllocWorkspace(alloc), alloc.getAgentSubmitFileName());
    }

	protected final String getSubmitContent(AllocationInfo alloc) {
    	Map<String, Object> ctx = new HashMap<String, Object>();
    	ctx.put("GWE_AGENT_ID", alloc.getId());
    	ctx.put("GWE_AGENT_SCRIPT", alloc.getAgentScriptFileName());
    	ctx.put("GWE_AGENT_WORKSPACE", getAllocWorkspace(alloc));
		return VelocityUtils.merge(ctx, template);
    }
}
