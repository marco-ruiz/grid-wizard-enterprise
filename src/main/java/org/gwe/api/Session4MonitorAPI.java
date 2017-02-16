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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.event.DefaultEventFilter;
import org.gwe.api.event.Event;
import org.gwe.api.event.EventFilter;
import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Oct 5, 2007
 */
public class Session4MonitorAPI extends Session4ServerAPI<ServerAPI4Monitor, Void> implements ISession4MonitorAPI {

	private static final long MINIMUM_DELAY_PERIOD = 3000;
	
	private static Log log = LogFactory.getLog(Session4MonitorAPI.class);

	private ExecutorService monitorsThreadPool = null;
	
	private Map<Long, EventQueueReader> eventQueues = new HashMap<Long, EventQueueReader>();

	private ServerAPI4Monitor monitorAPI;
	
	public Session4MonitorAPI(ServerAPILink accessor) throws ServerAPIConnectionException {
		super(ServerAPI4Monitor.class, accessor, null);
		monitorAPI = getServerAPI();
		monitorsThreadPool = ThreadPoolUtils.createThreadPool("Event Monitors", false);
	}
	
	public void abortMonitorEventsFor(long handle) {
		try {
			monitorAPI.abortCurrentMonitoring(handle);
		} catch (RemoteException e) {
        }
	}
	
	public void monitorEvents(EventListener listener) throws RemoteException {
		monitorEvents(listener, new DefaultEventFilter(null));
	}
	
	public void monitorEvents(EventListener listener, EventFilter filter) throws RemoteException {
		monitorEvents(MINIMUM_DELAY_PERIOD, listener, filter);
	}
	
	public void monitorEvents(long millisRefresh, EventListener listener, EventFilter filter) throws RemoteException {
		long handle = monitorAPI.createHandle();
		EventQueueReader queueReader = eventQueues.get(handle);
		if (queueReader == null) {
			queueReader = new EventQueueReader(handle, Math.max(MINIMUM_DELAY_PERIOD, millisRefresh), listener, filter);
			eventQueues.put(handle, queueReader);
		} else {
			queueReader.setNotifParameters(listener, filter);
			abortMonitorEventsFor(handle);
		}
		
		monitorsThreadPool.submit(queueReader);
	}
	
	public void shutdown() {
		monitorsThreadPool.shutdownNow();
	}
	
	class EventQueueReader implements Runnable {
		
		private long handle;
		private long delay;
		private EventListener listener;
		private EventFilter filter;

		public EventQueueReader(long handle, long refreshDelay, EventListener listener, EventFilter filter) {
			this.handle = handle;
			this.delay = refreshDelay;
			setNotifParameters(listener, filter);
		}

		public synchronized void setNotifParameters(EventListener listener, EventFilter filter) {
			this.listener = listener;
			this.filter = filter;
		}

		public void run() {
			while (true) {
				try {
					synchronized (this) {
						List<Event> newEvents = monitorAPI.getNextEvents(handle, filter);
						for (Event ev : newEvents) 
							try {
								listener.eventPerformed(ev);
							} catch (Exception e) {
//								log.debug(e);
							}
					}
				} catch (Exception e) {
					log.debug(e);
				}
				
				// Make a mandatory pause to do not overflow network
				try {
					Thread.yield();
					Thread.sleep(delay);
					Thread.yield();
				} catch (InterruptedException e) {}
			}
		}
	}
}
