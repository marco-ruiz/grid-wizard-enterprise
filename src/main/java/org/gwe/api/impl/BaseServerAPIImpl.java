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

package org.gwe.api.impl;

import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPILink;
import org.gwe.api.exceptions.ServerAPIBindingException;
import org.gwe.app.daemon.domain.BaseDomain;
import org.gwe.drivers.netAccess.tunneling.TunneledSocketFactory;
import org.gwe.persistence.model.HeadResourceInfo;

/**
 * 
 * @author Marco Ruiz
 * @since Oct 3, 2007
 */
public abstract class BaseServerAPIImpl<API_TYPE extends Remote, DOM_TYPE extends BaseDomain> implements Remote {

	private static Log log = LogFactory.getLog(BaseServerAPIImpl.class);
	
	protected Class<API_TYPE> remoteInterface;
	protected DOM_TYPE domain;

	public BaseServerAPIImpl() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.remoteInterface = (Class<API_TYPE>) genericSuperclass.getActualTypeArguments()[0];
	}

	public DOM_TYPE getDomain() {
		return domain;
	}

	public void setDomain(DOM_TYPE domain) {
		this.domain = domain;
	}

	protected Remote createExportableServerObject() { return this; }
	
	public API_TYPE bind(HeadResourceInfo daemonInfo) throws ServerAPIBindingException {
		TunneledSocketFactory daemonSocketFactory = new TunneledSocketFactory();
		API_TYPE stub;
        Remote exportableObj = createExportableServerObject();
        try {
			stub = (API_TYPE) UnicastRemoteObject.exportObject(exportableObj, 0, daemonSocketFactory, RMISocketFactory.getDefaultSocketFactory());
        } catch (RemoteException e) {
			throw new ServerAPIBindingException("Error encountered while trying to export server API " + exportableObj.getClass() + " to RMI registry", e);
        }
		
		String location = ServerAPILink.getFullRegistryId(daemonInfo, remoteInterface);
		log.info("Binding remote object of class '" + getClass().getName() + "' to RMI registry: '" + location + "'");
		try {
			Naming.rebind(location, stub);
		} catch (MalformedURLException e) {
			throw new ServerAPIBindingException("URL '" + location + "' not valid to bind '" + stub.getClass() + "' server API", e);
		} catch (RemoteException e) {
			throw new ServerAPIBindingException("RMI registry not reachable to bind " + stub.getClass() + " server API", e);
        }
		log.info("Remote stub object of class '" + stub.getClass().getName() + "' bound to RMI registry: '" + location + "'!");
		return stub;
	}
}
