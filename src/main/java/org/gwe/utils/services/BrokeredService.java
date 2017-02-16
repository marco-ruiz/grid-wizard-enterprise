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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class BrokeredService<RETURN_TYPE> {

	private static Log log = LogFactory.getLog(BrokeredService.class);

	protected ProcessingPermitBroker mgr;
	private String name;

	public BrokeredService(String name) {
		this(5, name);
	}

	public BrokeredService(int maxParallelRequest, String name) {
		mgr = new ProcessingPermitBroker(maxParallelRequest, name);
		this.name = name;
	}

	public final String getServiceName() {
		return name;
	}

	public final RETURN_TYPE processRequestBlocking(PlainService<RETURN_TYPE> service) throws Exception {
		// Do resolution processing here... the server is mostly idle waiting
		// for requests served flags
		long reqId = mgr.createPermit();
		log.debug("Permit [" + reqId + "] issued to process service [" + service + "]");
		try {
			RETURN_TYPE result = service.runService();
			mgr.destroyPermit(reqId);
			return result;
		} catch(Exception e) {
			mgr.destroyPermit(reqId);
			log.warn("Exception encountered while trying to process a brokered service", e);
			throw e;
		}
	}
}
