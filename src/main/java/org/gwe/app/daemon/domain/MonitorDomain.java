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

package org.gwe.app.daemon.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.api.event.Event;
import org.gwe.api.event.EventFilter;
import org.gwe.persistence.model.BaseModelInfo;
import org.gwe.persistence.model.EventType;
import org.gwe.persistence.model.IEventLogger;
import org.gwe.utils.concurrent.BlockingList;

/**
 * 
 * @author Marco Ruiz
 * @since Oct 4, 2007
 */
public class MonitorDomain extends BaseDomain implements Runnable, IEventLogger {
	
	private static final long MAXIMUM_BLOCKING_PERIOD = 10000;

	private Map<Long, UserEventNotifier> notifiers = new HashMap<Long, UserEventNotifier>();
	private BlockingList<Event> events = new BlockingList<Event>();
	
	public <KEY_TYPE> Timestamp logEvent(BaseModelInfo<KEY_TYPE> source, EventType evType, Timestamp when, BaseModelInfo... relatedModels) {
		events.add(new Event(evType, source, relatedModels));
		return when;
	}
	
	public long createHandle() {
		// TODO: Create a stronger unique handle for monitoring 
		return System.currentTimeMillis();
	}

	public List<Event> getNextEvents(long handle, EventFilter filter) {
		return getUserEventNotifier(handle).getNextEvents();
	}

	private UserEventNotifier getUserEventNotifier(long handle) {
		synchronized (notifiers) {
			UserEventNotifier notif = notifiers.get(handle);
			if (notif == null) {
				notif = new UserEventNotifier(handle);
				notifiers.put(handle, notif);
			}
			return notif;
		}
	}
	
	public void disposeNotifier(long handle) {
		synchronized (notifiers) { notifiers.remove(handle); }
	}

	public void run() {
		List<Event> eventsCopy;
		while (true) {
			eventsCopy = events.takeAll();
			if (!eventsCopy.isEmpty()) {
				Collection<UserEventNotifier> notifiersCopy;
				synchronized (notifiers) { notifiersCopy = notifiers.values(); }
				for (UserEventNotifier notif : notifiersCopy) notif.postEvents(eventsCopy);
			}
		}
	}

	class UserEventNotifier {
		
		private Long monitoringHandle;
		private List<Event> userEvents = new ArrayList<Event>();
		
		private UserEventNotifier(long monitoringHandle) {
			this.monitoringHandle = monitoringHandle;
		}
		
		private List<Event> getNextEvents() {
			List<Event> result = new ArrayList<Event>();
			synchronized (monitoringHandle) {
				while (userEvents.isEmpty())
					try { monitoringHandle.wait(MAXIMUM_BLOCKING_PERIOD); } catch (InterruptedException e) {}
				result.addAll(userEvents);
				userEvents.clear();
			}
			return result;
		}
		
		private void postEvents(List<Event> events) {
			synchronized (monitoringHandle) { 
				userEvents.addAll(events);
				monitoringHandle.notify();
			}
		}
	}
}
