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

package org.gwe.utils.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class ProcessingPermitBroker implements Runnable {
	private static Log log = LogFactory.getLog(ProcessingPermitBroker.class);

	private static ThreadGroup threadGroup = new ThreadGroup("Brokered Services"); 
	
	private List<PermitRequest> pendingRequests = new ArrayList<PermitRequest>();
	private Map<Long, PermitRequest> processingRequests = new TreeMap<Long, PermitRequest>();
//	private Map<Long, PermitRequest> completedRequests = new TreeMap<Long, PermitRequest>();

	private int maxParallel;

	public ProcessingPermitBroker(String name) {
		this(0, name);  // 0 means no maximum
	}

	public ProcessingPermitBroker(int maxParallelRequest, String name) {
		this.maxParallel = maxParallelRequest;
		Thread brokerThread = new Thread(threadGroup, this, name + " Permit's Broker");
		brokerThread.setDaemon(true);
		brokerThread.start();
	}

	public synchronized void run() {
		while (true) {
			while (pendingRequests.size() == 0 || (maxParallel > 0 && processingRequests.size() >= maxParallel)) {
				try {
					log.debug("Manager thread is going to sleep...");
					this.wait();
					log.debug("Manager thread woke up!");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}

			PermitRequest handle = pendingRequests.remove(0);
			log.debug("Manager is granting permission to request [" + handle + "]");
			processingRequests.put(handle.getId(), handle);
			synchronized (handle.getLock()) {
				handle.getLock().notifyAll();
			}
		}
	}

	public long createPermit() {
		log.debug("Client is soliciting processing permit");
		PermitRequest handle = PermitRequest.createDescriptor();
		synchronized (handle.getLock()) {
			try {
				addHandle(handle);
				handle.getLock().wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
		log.debug("Client has been granted processing permission for request [" + handle + "]");
		return handle.getId();
	}

	private synchronized void addHandle(PermitRequest handle) {
		log.debug("Adding request [" + handle + "] to waiting queue. Waking up manager thread...");
		pendingRequests.add(handle);
		this.notifyAll();
	}

	public synchronized void destroyPermit(long reqId) {
		PermitRequest handle = processingRequests.remove(reqId);
		log.debug("Client is reporting that is done with permit [" + handle + "]. Waking up manager thread...");
//		completedRequests.put(handle.getId(), handle);
		this.notifyAll();
	}
}
