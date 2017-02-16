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

package org.gwe.drivers.bundleManagers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

/**
 * @author Marco Ruiz
 * @since Nov 9, 2007
 */
public class BundleHandle {
	
    private static Log log = LogFactory.getLog(BundleHandle.class);
	
	protected BundleType type;
	protected String path;
	protected String name;

	public BundleHandle(BundleType type, String name) {
		setType(type);
		setPath(IOUtils.getFilePath(name));
		setName(IOUtils.getFileName(name));
	}

	public BundleHandle(BundleType type, String path, String name) {
		setType(type);
		setPath(path);
		setName(type.createName(name));
	}

	public BundleType getType() {
		return type;
	}

	public void setType(BundleType type) {
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getUnbundleCommand() {
		return type.getUnbundleCommand() + " " + getName();
	}
	
	public ShellCommand getUnbundleShellCommand() {
		return new ShellCommand(type.getUnbundleCommand() + " " + getName(), getPath(), null);
	}
	
	public boolean deploy(ResourceLink<HostHandle> destLink, String deploymentRoot, KeyStore keys) throws DeploymentException {
		return deploy(destLink, IOUtils.getFilePath(deploymentRoot), deploymentRoot, keys);
	}
	
	public boolean deploy(ResourceLink<HostHandle> destLink, String unbundleCmdPath, String deploymentRoot, KeyStore keys) throws DeploymentException {
		String fullPath = getFullPath();
		
		// Verify destination folder does not exist
		String deploymentDir = ThinURI.asNormalizedFileURI(destLink.getURIHost(), deploymentRoot);
        
		if (existsRemoteDirectory(deploymentDir, keys)) {
			log.info("Cannot deploy '" + fullPath + "' because target directory '" + deploymentDir + "' already exists. This bundle may have been already deployed there.");
			return false;
		}
		
		// Transfer bundle
		String destFile = transferTo(destLink, unbundleCmdPath, keys);
		
		// Unbundle bundle
		HostHandle conn;
        try {
	        conn = destLink.createHandle();
        } catch (HandleCreationException e) {
        	throw new DeploymentException("connect to remote host to unbundle file '" + destFile + "'", e);
        }
        
        try {
    		String result = conn.runCommand(getUnbundleCommand(), unbundleCmdPath, null);
    		conn.close();
			log.info("'" + fullPath + "' deployment results:\n" + result);
        } catch (ConnectorException e) {
        	throw new DeploymentException("execute remote command to unbundle file '" + destFile + "'", e);
        } catch (HandleOperationException e) {
        	// Ignore, bundle already unbundled
        }
		
		// Delete bundle
		deleteFile(destFile, keys);
		return true;
	}

	private boolean existsRemoteDirectory(String deploymentDir, KeyStore keys) throws DeploymentException {
	    try {
			return keys.createFileLink(deploymentDir).createHandle().isDirectory();
        } catch (HandleCreationException e) {
        	throw new DeploymentException("create handle to potential remote daemon installation directory '" + deploymentDir + "'", e);
        } catch (HandleOperationException e) {
        	throw new DeploymentException("inspect remote daemon installation directory '" + deploymentDir + "'", e);
        }
    }

	public String transferTo(ResourceLink<HostHandle> destLink, String destPath, KeyStore keys) throws DeploymentException {
		String destFile = ThinURI.asNormalizedFileURI(destLink.getURIHost(), IOUtils.concatenatePaths(destPath, getName()));
		try {
	        GWEAppContext.getGridFileSystem().transferFile(getFullPath(), destFile, keys);
        } catch (Exception e) {
        	throw new DeploymentException("transfer file '" + getFullPath() + "' to remote destination '" + destFile + "' ", e);
        }
	    return destFile;
    }

	private void deleteFile(String destFile, KeyStore keys) throws DeploymentException {
	    try {
			keys.createFileLink(destFile).createHandle().delete();
        } catch (HandleCreationException e) {
        	throw new DeploymentException("create handle to remote file '" + destFile + "'", e);
        } catch (HandleOperationException e) {
        	throw new DeploymentException("delete remote file '" + destFile + "'", e);
        }
    }

	private String getFullPath() {
	    return IOUtils.concatenatePaths(getPath(), getName());
    }
}
