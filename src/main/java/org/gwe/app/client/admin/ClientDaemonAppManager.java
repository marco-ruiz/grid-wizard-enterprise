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

package org.gwe.app.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.bundleManagers.BundleHandle;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.drivers.bundleManagers.DeploymentException;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OSAppFolder;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.ResourceLink;

public class ClientDaemonAppManager {

	private static Log log = LogFactory.getLog(ClientDaemonAppManager.class);

	// Daemon info
	private DaemonConfigDesc daemonConfig;

	private String localBundlesPath;

	public ClientDaemonAppManager(DaemonConfigDesc daemonConfig) {
		this.daemonConfig = daemonConfig;
		this.localBundlesPath = OSAppFolder.BUNDLES.getRelativeTo(ClientConfig.extractGWE_HOME());
	}
	
	public DaemonConfigDesc getDaemonConfig() {
		return daemonConfig;
	}
	
	public void installDaemonIfMissing() throws DeploymentException, RemoteExecutionException {
		if (deployDaemon()) setupDaemon();
	}
	
	public boolean deployDaemon() throws DeploymentException {
		return deployDaemon(BundleType.ZIP);
	}
	
	public boolean deployDaemon(BundleType bundleType) throws DeploymentException {
		ResourceLink<HostHandle> destLink = daemonConfig.getDaemonHostLink();
		HeadResourceInfo daemonInfo = daemonConfig.getHeadResource();
		
		// Resolve daemon bundle and destination
		String bundleName = IOUtils.concatenate(daemonConfig.getDistribution().getVersionedName(), "-daemon");
		BundleHandle bundle = new BundleHandle(bundleType, localBundlesPath, bundleName);
		
		// Deploy daemon
		return bundle.deploy(destLink, daemonInfo.getInstallPath(), daemonConfig.getKeys());
	}
	
	public boolean setupDaemon() throws RemoteExecutionException {
		HeadResourceInfo daemonInfo = daemonConfig.getHeadResource();
		String configRemotePath = daemonInfo.toFileProtocol(daemonInfo.getInstallation().getConfigurationFilePath());

		try {
			FileHandle serLinkHandle = daemonConfig.getKeys().createFileLink(configRemotePath).createHandle();
			// TODO: Make aborting of setup phase more robust than just checking for serialized link
			// DaemonApp should know when to re-install database and such.
			if (serLinkHandle.exists()) return false; 
			serLinkHandle.storeObject(daemonConfig);
        } catch (HandleOperationException e) {
        	throw new RemoteExecutionException("transfer serialized link to remote destination '" + configRemotePath + "' ", e);
        }
		return true;
	}

	public void launchDaemon() throws RemoteExecutionException {
		ShellCommand cmd = daemonConfig.getHeadResource().createDaemonLauncherCommand();
        String result = runCommandOnDaemonsHost(cmd);
        if (!result.contains(cmd.getExitToken())) { 
        	log.warn("Command '" + cmd.getCmd() + "' failed to execute." + "\n\n" + result.replace("\n", "\n\t"));
        	throw new RemoteExecutionException("run launch command '" + cmd.getCmd() + "'", new Exception());
        }
	}
	
	private String runCommandOnDaemonsHost(ShellCommand cmd) throws RemoteExecutionException {
        ResourceLink<HostHandle> nodeLink = daemonConfig.getDaemonHostLink();
        try {
			return nodeLink.createHandle().runCommand(cmd);
        } catch (HandleCreationException e) {
        	throw new RemoteExecutionException("connect to remote host '" + nodeLink.getURI() + "' to launch daemon", e);
        } catch (ConnectorException e) {
        	throw new RemoteExecutionException("execute launch command '" + cmd.getCmd() + "'", e);
        }
    }
}

