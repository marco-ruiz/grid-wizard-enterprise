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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Feb 3, 2008
 */
public class ShutdownDaemonRequest extends SystemDaemonRequest<String> {

	private static Log log = LogFactory.getLog(ShutdownDaemonRequest.class);

	private int reason;
	private Exception e;
	
	public ShutdownDaemonRequest(int releaseReason, Exception e) {
		this.reason = releaseReason;
		this.e = e;
	}
	
	public boolean systemProcess() {
		log.info("GWE daemon requested agent to shutdown. Release reason code = " + reason + ". Triggering Exception = ", e);
		return false;
	}
}
