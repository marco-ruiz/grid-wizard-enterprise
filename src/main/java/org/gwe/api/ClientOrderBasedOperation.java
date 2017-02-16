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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author Marco Ruiz
 * @since Dec 17, 2007
 */
public enum ClientOrderBasedOperation {

	PAUSE("pause-order"),
	RESUME("resume-order"), 
	ABORT("abort-order"),
	DELETE("delete-order");
	
	private String id;
	ClientOrderBasedOperation(String name) { this.id = name;}
	public String getId() { return id; }

	private static Map<String, ClientOrderBasedOperation> operations = new HashMap<String, ClientOrderBasedOperation>();
	static {
		for (ClientOrderBasedOperation oper : ClientOrderBasedOperation.values()) operations.put(oper.getId(), oper);
	}
	
	public static ClientOrderBasedOperation getOperation(String id) { return operations.get(id); }
	
	public static Set<String> getOperations() { return operations.keySet(); }
}
