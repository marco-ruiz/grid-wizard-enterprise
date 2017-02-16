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

package org.gwe.persistence.model.live;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Feb 22, 2008
 */
public class LiveOrders {

	private Map<Integer, OrderLive> orderLiveMap = new HashMap<Integer, OrderLive>();
	private DaemonConfigDesc config;
	
	public void setConfig(DaemonConfigDesc config) {
		this.config = config;
    }

	public synchronized void add(List<OrderInfo> newOrders) {
		for (OrderInfo order : newOrders) 
			orderLiveMap.put(order.getId(), new OrderLive(config, order));
	}

	public synchronized OrderLive get(int orderId) {
		return orderLiveMap.get(orderId);
	}

	public synchronized void dispose(int orderId) {
		OrderLive orderLive = orderLiveMap.remove(orderId);
		if (orderLive != null) orderLive.unload();
	}

	public synchronized Set<Integer> getJobsOrdersNotStarted(List<JobInfo> jobs) {
		int orderId;
		Set<Integer> result = new HashSet<Integer>();
		for (JobInfo job : jobs) {
			orderId = job.getOrderId();
			if (!orderLiveMap.containsKey(orderId)) result.add(orderId);
		}
		return result;
	}

	public synchronized Map<JobInfo, OrderLive> getJobLiveParams(List<JobInfo> jobs) {
		Map<JobInfo, OrderLive> result = new HashMap<JobInfo, OrderLive>();
		for (JobInfo job : jobs) 
			result.put(job, orderLiveMap.get(job.getOrderId()));
		return result;
	}
}
