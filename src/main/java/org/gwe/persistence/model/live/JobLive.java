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

import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderExecutionProfileInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.OrderDescriptor;

/**
 * @author Marco Ruiz
 * @since Aug 19, 2008
 */
public class JobLive {
	
	private JobInfo info;
	private OrderLive orderLive;
	
	public JobLive(JobInfo job, OrderLive orderLive) {
	    this.info = job;
	    this.orderLive = orderLive;
    }

	public JobInfo getInfo() {
    	return info;
    }
	
	public OrderLive getOrderLive() {
    	return orderLive;
    }
	
	public OrderInfo getOrderInfo() {
		return orderLive.getInfo();
	}
	
	public String getWorkspacePath() {
		return info.getWorkspaceInDaemon(orderLive.getConfig());
	}
	
	public boolean canCleanUp() {
    	OrderExecutionProfileInfo execProfile = orderLive.getInfo().getExecutionProfile();
		return execProfile.isCleanUpModeAlways() || (execProfile.isCleanUpModeOnSuccess() && info.getExecution() != null);
	}
	
	public <OD_TYPE extends OrderDescriptor> OD_TYPE getOrderDescriptor() {
	    return (OD_TYPE) getOrderInfo().getDescriptor();
    }
}
