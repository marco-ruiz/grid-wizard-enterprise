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

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.app.daemon.domain.UserDomain;
import org.gwe.persistence.dao.DaemonConfigDescDAO;
import org.gwe.persistence.model.DaemonInstallation;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OSAppFolder;

/**
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class DaemonApp {

	private static Log log = LogFactory.getLog(DaemonApp.class);

	public static final int DEFAULT_RMI_REGISTRY_PORT = 1099;
	public static final String DAEMON_APP_MAIN_COMPLETED_MSG = "DAEMON_APP_MAIN_COMPLETED";
	public static final String SPRING_DAEMON_CONF = "spring-gwe-daemon.xml";

	public static void main(String[] args) {
		DaemonBeacon beacon = new DaemonBeacon();
		try {
			new DaemonApp().executeApp(args[0]);
			beacon.setStop(true);
			System.out.println("\n" + DAEMON_APP_MAIN_COMPLETED_MSG);
		} catch(Exception e) {
			log.fatal("Daemon application failed to launch.", e);
			System.exit(1);
		}
	}
	
	private void executeApp(String installPath) throws Exception {
		install(installPath);
		
		GWEAppContext ctx = new GWEAppContext(DaemonApp.class, installPath, OSAppFolder.WORKSPACE.getRelativeTo(installPath), SPRING_DAEMON_CONF);
		DaemonConfigDescDAO configDescDAO = ctx.getBeanOfClass(DaemonConfigDescDAO.class);
		final UserDomain domain = ctx.getBeanOfClass(UserDomain.class);
		if (configDescDAO.isDatabaseScheduledForCreationOnThisRun())
			domain.setupDaemon(configDescDAO.getConfiguration().getHeadResource());
		
	    verifyRegistryIsAvailable();
	    configDescDAO.getConfiguration().getHeadResource().setRegistryPort(DEFAULT_RMI_REGISTRY_PORT);
	    DaemonAppStarter appStarter = ctx.getBeanOfClass(DaemonAppStarter.class);
		appStarter.registerRMIServerObjects();
		appStarter.startServices();
		
	    // FIXME: FOR TEST PURPOSES ONLY!!!
//		new QueueOrderTest(domain);
    }

	private void install(String installPath) throws Exception {
		if (new DaemonConfigDescDAO(installPath).isDatabaseScheduledForCreationOnThisRun())
			HeadResourceInfo.createDaemonScriptCommand(installPath, DaemonInstallerApp.class, -1).runLocally();
    }
	
	private void install2(String installPath) {
	    // Create system directories
		new DaemonInstallation(installPath).createDirectories();
    }
	
	private void verifyRegistryIsAvailable() throws DaemonAppException {
		int port = DEFAULT_RMI_REGISTRY_PORT;
		try {
			LocateRegistry.createRegistry(port);
			return;
		} catch (Exception e) {
			try {
				Registry registry = LocateRegistry.getRegistry();
				return;
			} catch (RemoteException e1) {
			}
		}
		throw new DaemonAppException("Could not locate/create RMI registry at port " + port);
	}
	
	class DaemonAppException extends Exception {
		private static final String MSG_PREFFIX = "Fatal exception while launching daemon. ";
		
		public DaemonAppException(String msg) { this(msg, null); }
		public DaemonAppException(String msg, Exception e) {
			super(MSG_PREFFIX + msg, e);
			log.fatal(MSG_PREFFIX + msg);
		}
	}
}

