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

import java.io.File;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Oct 29, 2008
 */
public class DaemonInstallation implements Serializable {
	
	private static Log log = LogFactory.getLog(DaemonInstallation.class);

	public static void createIfNonExistent(String dirPathStr) {
		File dirPath = new File(dirPathStr);
	    if (!dirPath.exists()) dirPath.mkdirs();
    }

	public static final String CONFIGURATION_FILE = "config.ser";
	public static final String PORT_FILE = "port.txt";
	
	private String installPath;
	private String workspacePath = null;
	private String allocsWorkspacePath = null;
	private String ordersWorkspacePath = null;
	private String installWorkspacePath = null;
	private String allocsDisposedWorkspacePath = null;

	public DaemonInstallation() {}

	public DaemonInstallation(String installPath) {
		setInstallPath(installPath);
    }

	public String getInstallPath() {
		return installPath;
    }

	public void setInstallPath(String installPath) {
    	this.installPath = installPath;
    	initialize();
    }

	private void initialize() {
		if (workspacePath != null) return; 
	    this.workspacePath = OSAppFolder.WORKSPACE.getRelativeTo(installPath);
    	this.allocsWorkspacePath         = IOUtils.concatenatePaths(workspacePath, "allocs");
    	this.allocsDisposedWorkspacePath = IOUtils.concatenatePaths(workspacePath, "allocs-disposed");
    	this.ordersWorkspacePath         = IOUtils.concatenatePaths(workspacePath, "orders");
    	this.installWorkspacePath        = IOUtils.concatenatePaths(workspacePath, "install");
    }

	public String getWorkspacePath() {
		initialize();
		return workspacePath;
	}

	public String getAllocsWorkspacePath() {
		initialize();
		return allocsWorkspacePath; 
	}
	
	public String getAllocsDisposedWorkspacePath() {
    	return allocsDisposedWorkspacePath;
    }

	public String getOrdersWorkspacePath() {
		initialize();
		return ordersWorkspacePath; 
	}

	public String getInstallationWorkspacePath() {
		initialize();
		return installWorkspacePath; 
	}

	public String getAllocsWorkspacePath(int id) {
		return IOUtils.concatenatePaths(getAllocsWorkspacePath(), id); 
	}
	
	public String getOrdersWorkspacePath(int id) {
		return IOUtils.concatenatePaths(getOrdersWorkspacePath(), id); 
	}
	
	public void disposeAllocFolder(int id) {
		try {
			String allocDispWSP = getAllocsDisposedWorkspacePath();
			String allocWS = getAllocsWorkspacePath(id);
			new File(allocDispWSP).mkdirs();
			ShellCommand.runLocally(ShellCommand.moveFile(allocWS, allocDispWSP));
		} catch(Exception e) {}
	}
	
	public void cleanupDisposedAllocFolder() {
		try {
			String cmd = ShellCommand.delete(getAllocsDisposedWorkspacePath());
			ShellCommand.runLocally(cmd);
			log.info("Disposed allocations folder deleted with command '" + cmd + "'");
		} catch(Exception e) {
			log.warn("Couldn't clean up disposed allocations folder");
		}
	}
	
	public String getConfigurationFilePath() {
		return getDBFilePath(CONFIGURATION_FILE);
	}

	public String getPortFilePath() {
		return getDBFilePath(PORT_FILE);
	}

	public String getDBFilePath(String fileName) {
		return IOUtils.concatenatePaths(OSAppFolder.DATABASE.getRelativeTo(installPath), fileName);
	}
	
	public void createDirectories() {
		createIfNonExistent(getWorkspacePath());
		createIfNonExistent(getAllocsWorkspacePath());
		createIfNonExistent(getOrdersWorkspacePath());
		createIfNonExistent(getInstallationWorkspacePath());
	}
}

