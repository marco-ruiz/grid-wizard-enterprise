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

/**
 * @author Marco Ruiz
 * @since Aug 27, 2008
 */
public class LiveOrdersProgress extends HashMap<Integer, Integer> {

	public synchronized void incrementJobsFinished(int orderId) {
    	Integer currentCount = get(orderId);
    	if (currentCount == null) currentCount = 0;
    	put(orderId, currentCount + 1);
    	notifyAll();
    }

	public synchronized OrderIncrementalProgress extractNextIncrementalProgress() {
		int nextOrderId = -1;
		int nextAmount = -1;
		while (isEmpty())
			try {
				wait();
			} catch (InterruptedException e) {}

		nextOrderId = keySet().iterator().next();
		nextAmount = get(nextOrderId);
		remove(nextOrderId);
		
		return new OrderIncrementalProgress(nextOrderId, nextAmount);
	}
}
