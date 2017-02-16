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
import org.gwe.app.client.ProgressTracker;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.drivers.bundleManagers.DeploymentException;
import org.gwe.persistence.model.DaemonConfigDesc;

/**
 * @author Marco Ruiz
 * @since Oct 2, 2008
 */
enum InstallationPhase { 
	deploy("deployed"), setup("setup"), launch("launched");
	
	private String pastTense;
	private InstallationPhase(String pastTense) { this.pastTense = pastTense; }
	public String getPastTense() { return pastTense; }
}

public class ClientDaemonInstaller {

    private static Log log = LogFactory.getLog(ClientDaemonInstaller.class);
	
	private ProgressTracker tracker;
	private ClientDaemonAppManager daemonAppManager;
	
	public ClientDaemonInstaller(ProgressTracker tracker, DaemonConfigDesc daemonConfig) {
	    this.tracker = tracker;
	    this.daemonAppManager = new ClientDaemonAppManager(daemonConfig);
    }

	public void setupDaemon() throws InstallerException {
		setupDaemon(BundleType.ZIP);
	}

	public void setupDaemon(BundleType bundleType) throws InstallerException {
		try {
	        installDaemon(bundleType);
    		tracker.trackProgress("GWE daemon running and ready!");
        } catch (InstallerException e) {
    		tracker.trackProgress(e.getMessage());
    		tracker.trackProgress("GWE daemon could not be launched.");
        	log.fatal(e);
        	throw e;
        }
	}

	public void installDaemon() throws InstallerException {
		installDaemon(BundleType.ZIP);
	}
	
	public void installDaemon(BundleType bundleType) throws InstallerException {
		tracker.trackProgress("Deploying bundle containing GWE daemon's distribution (this may take a few minutes)...");
		boolean newlyDeployed;
		try {
			newlyDeployed = daemonAppManager.deployDaemon(bundleType);
			trackPhaseProgress(InstallationPhase.deploy, newlyDeployed);
		} catch (DeploymentException e) {
			throw new InstallerException(InstallationPhase.deploy, e);
        }
		
		if (newlyDeployed) {
			try {
				tracker.trackProgress("Setting up GWE daemon (this may take a few minutes)...");
	            boolean setup = daemonAppManager.setupDaemon();
	            trackPhaseProgress(InstallationPhase.setup, setup);
            } catch (RemoteExecutionException e) {
            	throw new InstallerException(InstallationPhase.setup, e);
            }
		}
		
       	try {
            tracker.trackProgress("Launching GWE daemon (this may take a few minutes)...");
	        daemonAppManager.launchDaemon();
	       	tracker.trackProgress("GWE daemon launched.");
        } catch (RemoteExecutionException e) {
        	throw new InstallerException(InstallationPhase.launch, e);
        }
	}
	
	private void trackPhaseProgress(InstallationPhase phase, boolean performedJustNow) {
		String performedTime = performedJustNow ? "" : "was already ";
		tracker.trackProgress("GWE daemon " + performedTime + phase.getPastTense() + ".");
	}
}
