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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.exceptions.PasswordMismatchException;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2007
 */
public class Session4ServerAPI<API_TYPE extends Remote, SESSION_ID> {

	private static Log log = LogFactory.getLog(Session4ServerAPI.class);

	private API_TYPE serverAPI;
	private Class<API_TYPE> serverAPIClass;
	private ServerAPILink apiLink;
	protected SESSION_ID sessionId;

	protected Session4ServerAPI(Class<API_TYPE> serverAPIClass, ServerAPILink apiLink, SESSION_ID sessionId) {
		this.serverAPIClass = serverAPIClass;
		this.apiLink = apiLink;
		this.sessionId = sessionId;
	}

	public <RETURN_TYPE> RETURN_TYPE executeRequest(SessionRequest<API_TYPE, RETURN_TYPE> req) throws ServerAPIConnectionException, RemoteException, PasswordMismatchException {
		try {
			try {
				return req.execute(getServerAPI());
			} catch (RemoteException e) {
				serverAPI = null;
				return req.execute(getServerAPI());
			}
		} catch (ServerAPIConnectionException e) {
			log.warn(e);
			throw e;
		}
	}
	
	public void connect() throws ServerAPIConnectionException { getServerAPI(); }
	
	protected API_TYPE getServerAPI() throws ServerAPIConnectionException {
		if (serverAPI == null) {
			String host = apiLink.getDaemonInfo().getHost();
			try {
            	serverAPI = apiLink.createAPIProxy(serverAPIClass);
            } catch (URISyntaxException e) {
            	throw new ServerAPIConnectionException("Make sure a valid URI can be built with host " + host, e);
            } catch (RemoteException e) {
            	throw new ServerAPIConnectionException("Make sure there are not network connectivity issues including invalid network level credentials to SSH into host " + host, e);
            } catch (NotBoundException e) {
            	throw new ServerAPIConnectionException("Make sure a GWE daemon is remotely installed at " + host, e);
            }
		}
		return serverAPI;
	}
}

