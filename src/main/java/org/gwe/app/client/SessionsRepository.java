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

package org.gwe.app.client;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPI;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.app.client.admin.ClientDaemonInstaller;
import org.gwe.app.client.admin.InstallerException;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;

/**
 * @author Marco Ruiz
 * @since Dec 16, 2008
 */
public class SessionsRepository extends HashMap<HeadResourceInfo, Session4ClientAPIEnhancer> {
	
    private static Log log = LogFactory.getLog(SessionsRepository.class);

	private ClientConfig config;
	private ProgressTracker tracker = ProgressTracker.LOG_TRACKER;;

	public SessionsRepository(ClientConfig config) {
	    this.config = config;
    }

	public void setTracker(ProgressTracker tracker) {
    	this.tracker = tracker;
    }

	public Session4ClientAPIEnhancer getSession(HeadResourceInfo daemonInfo) throws InstallerException, ServerAPIConnectionException {
		return getSession(daemonInfo, false);
	}
	
	public Session4ClientAPIEnhancer getSession(HeadResourceInfo daemonInfo, boolean installDaemonIfNeeded) throws InstallerException, ServerAPIConnectionException {
		return getSession(daemonInfo, installDaemonIfNeeded, BundleType.ZIP);
	}
	
	public Session4ClientAPIEnhancer getSession(HeadResourceInfo daemonInfo, boolean installDaemonIfNeeded, BundleType bundleType) throws InstallerException, ServerAPIConnectionException {
	    if (daemonInfo == null) return null;
		Session4ClientAPIEnhancer result = getTestedSession(daemonInfo);
		if (result == null) {
			result = createClientSession(daemonInfo, installDaemonIfNeeded, bundleType);
			put(daemonInfo, result);
		}
		return result;
    }

	private Session4ClientAPIEnhancer getTestedSession(HeadResourceInfo selectedCluster) {
		Session4ClientAPIEnhancer result = null;
	    try {
			result = get(selectedCluster);
			if (result != null) 
		        result.getSession().connect();
	    } catch (ServerAPIConnectionException e) {
	    	put(selectedCluster, null);
	    }
        return result;
    }
	
	private Session4ClientAPIEnhancer createClientSession(HeadResourceInfo headResource, boolean installDaemonIfNeeded, BundleType bundleType) throws InstallerException, ServerAPIConnectionException {
		DaemonConfigDesc daemonConfig = config.createDaemonConfig(headResource);
		Session4ClientAPI session = new Session4ClientAPI(daemonConfig);
		
		try {
			tracker.trackProgress("Connecting to GWE daemon: " + daemonConfig.getConnectionURL() + " ...");
            session.connect();
        } catch (ServerAPIConnectionException e) {
			tracker.trackProgress("Remote daemon not installed or running!");
        	if (!installDaemonIfNeeded) throw e;
        	new ClientDaemonInstaller(tracker, daemonConfig).setupDaemon(bundleType);
        }
		
		return new Session4ClientAPIEnhancer(session);
	}
}

