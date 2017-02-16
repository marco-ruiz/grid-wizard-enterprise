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

import java.util.Date;

/**
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class PermitRequest {

	private static long count = 0;

	public static PermitRequest createDescriptor() {
		return new PermitRequest(count++);
	}

	private long id;
	private Object lock;
	private long creationTime;
	private long scheduledTime;
	private long completionTime;

	private PermitRequest(long id) {
		this.id = id;
		this.lock = new Object();
		creationTime = System.currentTimeMillis();
	}

	public void requestScheduled() {
		scheduledTime = System.currentTimeMillis();
	}

	public void requestCompleted() {
		completionTime = System.currentTimeMillis();
	}

	public Object getLock() {
		return lock;
	}

	public long getCompletionTime() {
		return completionTime;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getId() {
		return id;
	}

	public long getScheduledTime() {
		return scheduledTime;
	}

	public String toString() {
		return "Id: " + id + ". Created @ " + new Date(creationTime) + ". Scheduled @ " + new Date(scheduledTime) 
				+ ". Completed @ " + new Date(completionTime) + "\n";
	}
}
