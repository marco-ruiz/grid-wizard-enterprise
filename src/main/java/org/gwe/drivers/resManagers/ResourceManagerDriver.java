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

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.agent.AgentApp;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.persistence.model.AllocationInfo;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Aug 8, 2007
 */
public abstract class ResourceManagerDriver {

	private static Log log = LogFactory.getLog(ResourceManagerDriver.class);

	public abstract boolean isSupportedJobManagerAvailable();

	protected String id;
	
	protected String agentScriptPreffix;
	protected DaemonConfigDesc configuration;
	
	public String getId() {
	    return id;
    }

	public void setId(String id) {
    	this.id = id;
    }

	public void setConfiguration(DaemonConfigDesc config) {
		this.configuration = config;
		String installPath = configuration.getHeadResource().getInstallation().getInstallPath();
		String baseScript = IOUtils.concatenatePaths(installPath, "/bin/gwed-base-script.sh");
		agentScriptPreffix = "#!/bin/bash\n " + baseScript + " " + AgentApp.class.getName() + " " + installPath + " ";
	}
	
	public String getAllocWorkspace(AllocationInfo alloc) {
		return configuration.getAllocWorkspace(alloc.getId());
	}

    public final void allocateComputeResource(AllocationInfo alloc) throws ResourceAllocationException {
		String workspace = getAllocWorkspace(alloc);
		new File(workspace).mkdir();
		log.info("Creating allocation " + alloc.getId() + " with workspace " + workspace);
		String systemPid = queueAllocationRequest(alloc);
        log.info("Allocation " + alloc.getId() + " created with process id = " + systemPid);
        alloc.setSystemPid(systemPid);
    }
    
	protected String[] runQueueCommand(String cmd, AllocationInfo alloc) throws ResourceAllocationException {
		log.trace("Command: " + cmd);
		try {
			String results = new ShellCommand(cmd, getAllocWorkspace(alloc), null).runLocally();
	        for (String line : results.split("\n")) { log.trace("R: " + line); }
			return results.split("\n");
		} catch (ConnectorException e) {
			throw new ResourceAllocationException("Could not run queue command '" + cmd + "'", e);
		}
	}
    
    // Must return the PID
    public abstract String queueAllocationRequest(AllocationInfo alloc) throws ResourceAllocationException;
    
	protected String writeAllocationFile(String fileName, String content, AllocationInfo alloc) throws ResourceAllocationException {
		try {
			String fullFileName = IOUtils.concatenatePaths(getAllocWorkspace(alloc), fileName);
			IOUtils.createLocalExecutableFile(fullFileName, content);
		    log.info("Wrote file '" + fileName + "' for allocation " + alloc.getId());
	        return fileName;
        } catch (IOException e) {
			String msg = "Could not write file " + fileName + " needed to launch agent for allocation " + alloc.getId();
			log.warn(msg, e);
			throw new ResourceAllocationException(msg, e);
        }
    }
    
	public abstract AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException;
	public abstract AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException;
}
