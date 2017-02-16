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

package org.gwe.app.daemon.domain.background;

import java.util.List;

import org.gwe.app.daemon.domain.AgentDomain;
import org.gwe.app.daemon.domain.DaemonDomain;
import org.gwe.app.daemon.domain.UserDomain;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Nov 6, 2007
 */
public class JobGenerator extends AgentDomainBasedService {

	private UserDomain clientDom;
	private DaemonDomain daemonDom;
	
	public JobGenerator(UserDomain clientDom, AgentDomain agentDom, DaemonDomain daemonDom) {
		super("Job Generator", agentDom);
		this.clientDom = clientDom; 
		this.daemonDom = daemonDom;
	}
	
	public void execute() {
		List<OrderInfo> orders = clientDom.dequeueUnprocessedOrders();
		orders = clientDom.generateJobs(orders);
//		daemonDom.assignDaemonsToJobs(orders);
		agentDom.triggerMoreJobsAndAllocationPreparation();
	}
}
