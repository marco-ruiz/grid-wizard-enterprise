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

import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPI4Monitor;
import org.gwe.api.event.Event;
import org.gwe.api.event.EventFilter;
import org.gwe.app.daemon.domain.MonitorDomain;

/**
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public class ServerAPI4MonitorImpl extends BaseServerAPIImpl<ServerAPI4Monitor, MonitorDomain> implements ServerAPI4Monitor {

	private static Log log = LogFactory.getLog(ServerAPI4MonitorImpl.class);
	
	//====================
	// Event Notification 
	//====================
	public long createHandle() throws RemoteException {
		return domain.createHandle();
	}

	public List<Event> getNextEvents(long handle, EventFilter filter) throws RemoteException {
		return domain.getNextEvents(handle, filter);
	}

	public void abortCurrentMonitoring(long handle) throws RemoteException {
		domain.disposeNotifier(handle);
	}
}
