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

package org.gwe.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public class RemoteResolver<RT extends Remote> implements Runnable {

	private static Log log = LogFactory.getLog(RemoteResolver.class);

	private RT remoteObj = null;
	private String[] servicesList = null;
	private RemoteException rex = null;
	private NotBoundException nbex = null;
	private URI uriObj;
	private Thread resolverThread = null;
	private boolean list = false;
	private RMIClientSocketFactory socketFactory;

	public RemoteResolver(String uriStr, RMIClientSocketFactory socketFactory) throws URISyntaxException, UnknownHostException {
		this.uriObj = new URI(uriStr);
		this.socketFactory = socketFactory;
	}

	public RT getRemote(long waitMillis) throws RemoteException, NotBoundException, ConnectionTimeoutException {
		return (RT)getRegistryInformation(waitMillis, false);
	}
	
	public String[] getList(long waitMillis) throws RemoteException, NotBoundException, ConnectionTimeoutException {
		return (String[])getRegistryInformation(waitMillis, true);
	}

	public synchronized <INFO_TYPE extends Object> INFO_TYPE getRegistryInformation(long waitMillis, boolean list) throws RemoteException, NotBoundException, ConnectionTimeoutException {
		if (resolverThread != null) return null; // TODO: Must throw exception!!!
		this.list = list;
		resolverThread = new Thread(this);
		resolverThread.start();
		long timeout;
		long end = System.currentTimeMillis() + waitMillis;
		while ((timeout = end - System.currentTimeMillis()) > 0) {
			try {
				this.wait(timeout);
			} catch (InterruptedException e) {}
			INFO_TYPE info = (INFO_TYPE) ((list) ? servicesList : remoteObj);
			if (info != null) return info;
			if (rex != null)  throw rex;
			if (nbex != null) throw nbex;
		}

		// RemoteResolver used will dispose itself automatically once it returns from its blocked state
		throw new ConnectionTimeoutException(uriObj.toString(), timeout);
	}

	public void run() {
		try {
			log.info("Retrieving remote registry '" + uriObj.getHost() + ":" + uriObj.getPort() + "'...");
			Registry registry = LocateRegistry.getRegistry(uriObj.getHost(), uriObj.getPort(), socketFactory);
			log.info("Registry obtained! " + registry);
			log.info("Retrieving remote object '" + uriObj.toString() + "'...");
			RT rObj = null;
			String[] sList = null;
			if (list)
				sList = registry.list();
			else
				rObj = (RT) registry.lookup(uriObj.getPath().substring(1));
			log.info("Remote proxy object created: '" + rObj + "'. Actual object at '" + uriObj.toString() + "'");
			synchronized (this) {
				remoteObj = rObj;
				servicesList = sList;
				close();
			}
		} catch (RemoteException e) {
			synchronized (this) {
				this.rex = e;
				close();
			}
		} catch (NotBoundException e) {
			synchronized (this) {
				this.nbex = e;
				close();
			}
		}
	}
	
	private synchronized void close() {
		resolverThread = null;
		this.notifyAll();
	}
	
}

class ConnectionTimeoutException extends Exception {
	
	public ConnectionTimeoutException(String uriStr, long timeout) {
		super("Timeout reached! No exception or sucessful response received from '" + uriStr + "' within " + timeout / 1000 + " seconds");
	}
}
