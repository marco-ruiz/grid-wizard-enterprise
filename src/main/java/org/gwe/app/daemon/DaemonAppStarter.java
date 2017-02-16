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

package org.gwe.app.daemon;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gwe.api.exceptions.ServerAPIBindingException;
import org.gwe.api.impl.BaseServerAPIImpl;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;

/**
 * @author Marco Ruiz
 * @since Nov 3, 2008
 */
public class DaemonAppStarter {
	
	private static final Set hackToPreventGarbageCollection = new HashSet();
	
	private ThreadGroup servicesGroup = new ThreadGroup("Background Services");
	
	private void startBackgroundService(Runnable serv, String name) {
		Thread threadObj = new Thread(servicesGroup, serv, "BG Service: " + name);
		threadObj.setDaemon(false);
		threadObj.start();
	}

	private DaemonConfigDesc configuration;
	private Set<BaseServerAPIImpl> serverAPIs;
	private Map<Runnable, String> daemonThreadServices;

	public void setConfiguration(DaemonConfigDesc configuration) {
    	this.configuration = configuration;
    }

	public void setServerAPIs(Set<BaseServerAPIImpl> serverAPIs) {
		this.serverAPIs = serverAPIs;
	}

	public void setDaemonThreadServices(Map<Runnable, String> daemonThreadServices) {
		this.daemonThreadServices = daemonThreadServices;
	}

	public void registerRMIServerObjects() throws ServerAPIBindingException {
		HeadResourceInfo daemonInfo = configuration.getHeadResource();
		
		// Bind server objects to RMI registry
		for (BaseServerAPIImpl<?, ?> api : serverAPIs)
			hackToPreventGarbageCollection.add(api.bind(daemonInfo));
    }

	public void startServices() {
	    // Start thread services
		for (Runnable serv : daemonThreadServices.keySet())
			startBackgroundService(serv, daemonThreadServices.get(serv));
    }
}

