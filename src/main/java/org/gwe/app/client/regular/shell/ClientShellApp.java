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

package org.gwe.app.client.regular.shell;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ClientOrderBasedOperation;
import org.gwe.api.ClientOrderBasedQuery;
import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.api.exceptions.GWEDomainException;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.client.GWEASCIILogo;
import org.gwe.app.client.ProgressTracker;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.config.XMLClientConfigReader;
import org.gwe.p2elv2.P2ELSyntaxException;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.StringUtils;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.security.CredentialNotFoundException;
import org.gwe.utils.security.Realm;
import org.gwe.utils.security.RealmTestResult;

/**
 * @author Marco Ruiz
 * @since Jan 25, 2008
 */
public class ClientShellApp {

	protected static final String QUEUE_ORDER_COMMAND = "queue-order";

	private static Log log = LogFactory.getLog(ClientShellApp.class);

	protected ProgressTracker tracker;
	protected ClientConfig appConfig;
	protected HeadResourceInfo cluster;

	public ClientShellApp(String clientName, ProgressTracker tracker, int index, ArgsList argsList) throws CredentialNotFoundException, IOException {
		this(clientName, tracker, argsList.extractArgIfPrefixed(index, XMLClientConfigReader.CONF_ARG_PREFIX));
	}

	public ClientShellApp(String clientName, ProgressTracker tracker, String confArg) throws CredentialNotFoundException, IOException {
		this.tracker = tracker;
		XMLClientConfigReader configReader = new XMLClientConfigReader(confArg);
		this.cluster = configReader.getHeadResource();
		printHeader(clientName);
		this.appConfig = new ClientConfig(configReader);
		printKeyStoreTestProblems(tracker);
		this.appConfig.setTracker(tracker);
	}

	private void printHeader(String clientName) {
		tracker.trackProgress("==============================================================");
		tracker.trackProgress(GWEASCIILogo.prefixLogo(GWEASCIILogo.GWE, "\t") + "\n");
		tracker.trackProgress("\tWelcome to the GWE " + clientName + " Application");
		tracker.trackProgress("\t\tGWE daemon descriptor selected: '" + cluster.getName() + "'");
		tracker.trackProgress("\t\tHost: " + cluster.getHost() + "");
		tracker.trackProgress("\t\tQueue Size: " + cluster.getQueueSize() + " nodes");
		tracker.trackProgress("\t\tHijack Timeout: " + toPrecision(cluster.getMaxHijackMins()) + " minutes");
		tracker.trackProgress("\t\tIdle Timeout: " + toPrecision(cluster.getMaxIdleMins()) + " minutes");
		tracker.trackProgress("\n==============================================================\n");
    }
	
	private void printKeyStoreTestProblems(ProgressTracker tracker) {
	    for (Realm realm : appConfig.getKeys().getRealms()) {
			RealmTestResult testResult = realm.getTestResult();
			if (testResult != RealmTestResult.OK)
				tracker.trackProgress("WARNING: Problems testing realm '" + realm + "' - [" + testResult.getMessage() + "]"  + "");
        }
    }
	
	private String toPrecision(float number) {
		int num100 = (int)(number * 100);
		return (int)num100/100 + "." + (int)num100%100;
	}

	public ClientConfig getAppConfig() {
    	return appConfig;
    }

	public Session4ClientAPIEnhancer getSession() {
		try {
			return appConfig.getSessionsRepository().getSession(cluster, true);
	    } catch (Exception e) {
	    	exit("");
	    }
	    return null;
	}

	public String processCommand(String[] args) throws ServerAPIConnectionException, P2ELSyntaxException, PasswordMismatchException, RemoteException, GWEDomainException, REXException {
		String cmdName = args[0];
		String[] actualArgs = StringUtils.removeArgs(args, 0, 1);
		String output = "Command '" + cmdName + "' not supported";
		
		Session4ClientAPIEnhancer session = getSession();
		if (QUEUE_ORDER_COMMAND.equals(cmdName)) {
			String stmt = StringUtils.getArrayAsStr(actualArgs);
			if (stmt.trim().equals("")) return "P2EL statement missing!";
			OrderInfo order = new OrderInfo(appConfig.createOrderDescriptor(stmt));
			OrderInfo queuedOrder = session.queueOrder(order);
			output = "Order queued for processing with id " + queuedOrder.getId();
		}
		
		if (ClientOrderBasedQuery.getQuery(cmdName) != null)
			output = session.doOrderBasedQuery(ClientOrderBasedQuery.getQuery(cmdName), getIdArg(0, actualArgs), getIdArg(1, actualArgs));
		
		if (ClientOrderBasedOperation.getOperation(cmdName) != null)
			output = session.doOrderBasedOperation(ClientOrderBasedOperation.getOperation(cmdName), getIdArg(0, actualArgs));
		
		return output;
    }
	
    protected List<String> getCommandList() {
		List<String> result = new ArrayList<String>();
		result.add(QUEUE_ORDER_COMMAND);
		result.addAll(ClientOrderBasedOperation.getOperations());
		result.addAll(ClientOrderBasedQuery.getQueries());
		return result;
	}
    
	private int getIdArg(int idx, String[] args) {
		try {
			return (idx > args.length - 1) ? -1 : Integer.parseInt(args[idx]);
		} catch(NumberFormatException e) {
			throw new RuntimeException("Argument '" + args[idx] + "' is not an integer");
		}
	}

	protected void exit(String message) {
        tracker.trackProgress((message + ".\nExiting."));
        System.exit(0);
	}
}

