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

package org.gwe.app.client.config;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.app.client.ProgressTracker;
import org.gwe.app.client.SessionsRepository;
import org.gwe.p2elv2.macro.PMacroLibrary;
import org.gwe.p2elv2.macro.PMacroRepo;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.GridInfo;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.order.OrderDescriptor;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.gwe.utils.IOUtils;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since Jun 14, 2007
 */
public class ClientConfig {

	private static final String MACRO_EXTENSION = ".p2el";

	private static Log log = LogFactory.getLog(ClientConfig.class);

	public static final String GWE_HOME_ENV_VAR = "GWE_HOME";

	public static String extractGWE_HOME() {
		String gweHome = System.getenv(GWE_HOME_ENV_VAR);
		return (gweHome == null || "".equals(gweHome)) ? "" : IOUtils.concatenatePaths(gweHome, "");
	}
	
	public static InstallationFiles getInstallFiles() {
		return new InstallationFiles(extractGWE_HOME());
	}
	
	protected GWEAppContext appContext;

	private GridInfo grid;
	private KeyStore keys;

	private PMacroRepo macroRepo = new PMacroRepo();
	private SessionsRepository sessionsRepository = new SessionsRepository(this); 
	
	public ClientConfig(String host, String daemonRootPath, String user, String password) {
		this(host, daemonRootPath, KeyStore.createKeyStore(new AccountInfo(user, password), host));
	}

	public ClientConfig(String host, String daemonRootPath, KeyStore keys) {
		this(new HeadResourceInfo(host, daemonRootPath), keys);
	}

	public ClientConfig(HeadResourceInfo clusterHeadNode, KeyStore keys) {
		this(new GridInfo(clusterHeadNode), keys);
	}
	
	public ClientConfig(ClientConfigReader configReader) {
		this(configReader.getGrid(), configReader.getKeys());
    }

	public ClientConfig(GridInfo grid, KeyStore keys) {
		String installPath = extractGWE_HOME();
		this.appContext = new GWEAppContext(ClientConfig.class, installPath, installPath);
		keys.test();
		this.keys = keys;
		this.grid = grid;
		
		String version = appContext.getDistribution().getVersion();
		for (HeadResourceInfo headRes : grid.getHeadResources()) headRes.setVersion(version);
		grid.validate();

		loadMacros(new File(getInstallFiles().getConfigFilePath("gwe-macros")).listFiles());
		log.info("Confguration read!");
//		addShutdownHooks();
	}

	private void addShutdownHooks() {
		Thread processReapersShutdown = new Thread(new Runnable() {
			public void run() {
				Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
				for (Thread thread : allStackTraces.keySet())
	                if (thread.isDaemon() && thread.getName().contains("process reaper"))
	                	thread.interrupt();
            }
		});
        Runtime.getRuntime().addShutdownHook(processReapersShutdown);
	}
	
	private void loadMacros(File[] macroFiles) {
		log.info("Loading P2EL macros...");
		for (File macroFile : macroFiles) {
	        if (macroFile.getName().endsWith(MACRO_EXTENSION)) {
	        	try {
                	String currLibCategory = macroFile.getName().replace(MACRO_EXTENSION, "").toUpperCase();
                	PMacroLibrary currLib = PMacroLibrary.read(macroFile.getCanonicalPath(), true);
                	currLib.setCategory(currLibCategory);
					macroRepo.add(currLib);
                } catch (Exception e) {
                	log.warn("Could not load macros from library '" + macroFile + "'", e);
                }
	        }
        }
		log.info("P2EL macros loaded!");
	}
	
	public void setTracker(ProgressTracker tracker) {
    	sessionsRepository.setTracker(tracker);
    }

	public GWEAppContext getAppContext() {
    	return appContext;
    }

	public void setAppContext(GWEAppContext appContext) {
    	this.appContext = appContext;
    }
	
	public GridInfo getGrid() {
		return grid;
	}
	
	public KeyStore getKeys() {
    	return keys;
    }

	public PMacroRepo getMacroRepo() {
    	return macroRepo;
    }

	public SessionsRepository getSessionsRepository() {
    	return sessionsRepository;
    }

	public DaemonConfigDesc createDaemonConfig(HeadResourceInfo headResource) {
	    return new DaemonConfigDesc(headResource, keys, appContext.getDistribution());
    }

	public OrderDescriptor<Serializable> createOrderDescriptor(String stmt) throws REXException {
		String parsedStmt = macroRepo.applyMacros(stmt.replaceAll("\\s{1,}", " ").trim());
		return new POrderDescriptor<Serializable>(parsedStmt);
    }
}

