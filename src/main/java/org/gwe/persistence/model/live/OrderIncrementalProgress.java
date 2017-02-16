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

/**
 * @author Marco Ruiz
 * @since Aug 27, 2008
 */
public class OrderIncrementalProgress {

	private int orderId;
	private int completedAmount;
	private int failedAmount;

	public OrderIncrementalProgress(int orderId, int completedAmount) {
		this(orderId, completedAmount, -1);
    }

	public OrderIncrementalProgress(int orderId, int completedAmount, int failedAmount) {
	    this.orderId = orderId;
	    this.completedAmount = completedAmount;
	    this.failedAmount = failedAmount;
    }

	public int getOrderId() {
    	return orderId;
    }

	public int getJobsCompletedIncrement() {
    	return completedAmount;
    }

	public int getJobsFailedIncrement() {
    	return failedAmount;
    }
}
