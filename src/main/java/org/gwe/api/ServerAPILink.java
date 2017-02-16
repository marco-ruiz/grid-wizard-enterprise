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

package org.gwe.api;

import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.tunneling.TunneledSocketFactory;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.utils.IOUtils;
import org.gwe.utils.concurrent.HeartBeater;
import org.gwe.utils.reinvoke.ReinvocationInterceptor;
import org.gwe.utils.security.ResourceLink;

/**
 * @author Marco Ruiz
 * @since Dec 13, 2007
 */
public class ServerAPILink {

    private static Log log = LogFactory.getLog(ServerAPILink.class);

	public static <RT> String getRegistryId(Class<RT> remoteClass) {
	    return remoteClass.getName().replace('.', '/');
    }

	public static <RT extends Remote> String getFullRegistryId(HeadResourceInfo daemonInfo, Class<RT> remoteClass) {
		return IOUtils.concatenatePaths(daemonInfo.getLocation(), getRegistryId(remoteClass));
	}

	private HeadResourceInfo daemonInfo;
	private ResourceLink<HostHandle> hostLink;
	
	public ServerAPILink(HeadResourceInfo daemonInfo, ResourceLink<HostHandle> link) {
		this.daemonInfo = daemonInfo;
        this.hostLink = link;
	}
	
	public HeadResourceInfo getDaemonInfo() {
		return daemonInfo;
	}

	public ResourceLink<HostHandle> getHostLink() {
		return hostLink;
	}

	public Session4MonitorAPI createEventMonitor() throws Exception {
		return new Session4MonitorAPI(this);
	}

	public <RT extends Remote> RT createAPIProxy(Class<RT> remoteClass) throws URISyntaxException, RemoteException, NotBoundException {
		String remoteObjURI = getFullRegistryId(daemonInfo, remoteClass);
		log.info("Trying to establish a link to " + remoteObjURI + " ...");
		Registry registry = LocateRegistry.getRegistry(daemonInfo.getHost(), daemonInfo.getRegistryPort(hostLink.getAccountInfo()), new TunneledSocketFactory());
		log.info("Registry obtained! " + registry + ". Retrieving " + remoteObjURI + " ...");
		String registryId = IOUtils.concatenatePaths("", daemonInfo.generateRMIPath(), getRegistryId(remoteClass)).substring(1);
		RT serverAPI = (RT) registry.lookup(registryId);
		log.info("Remote proxy object created: '" + serverAPI + "' for " + remoteObjURI);
		// Add reinvocation aspect before the optional beating aspect
		serverAPI = ReinvocationInterceptor.createProxy(serverAPI);
		return PulsingServerAPIProxyCreator.createProxyIfNecessary(serverAPI, new RemoteInvocatorHeartBeater());
	}

	class RemoteInvocatorHeartBeater implements HeartBeater<PulsingServerAPI> {
		public boolean beatAndReportIfMustContinue(Object id, PulsingServerAPI heartContainer) {
			try {
	            heartContainer.heartBeat(id);
	        } catch (RemoteException e) {
	        }
			return true;
		}
	};
}


