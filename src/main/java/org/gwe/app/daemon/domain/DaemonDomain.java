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

import java.util.List;

import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Oct 3, 2007
 */
public class DaemonDomain extends BaseDomain {

	public void assignDaemonsToJobs(List<OrderInfo> orders) {
		HeadResourceInfo daemonInfo = getConfig().getHeadResource();
		for (OrderInfo order : orders) {
/*
			if (order.getGrid().contains(daemonInfo)) {
				for (JobInfo job : order.getJobs()) {
					job.setRunningDaemon(daemonInfo);
				}
			}
*/
		}
	}

}
