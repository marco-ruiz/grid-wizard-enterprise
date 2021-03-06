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

package org.gwe.integration.slicer.chains;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Ruiz
 * @since May 28, 2008
 */
public class ChainCLMDesc {
	
	private String id;
	private Map<String, String> pipedArguments = new HashMap<String, String>();

	public ChainCLMDesc(String id, Map<String, String> pipedArguments) {
	    this.id = id;
	    this.pipedArguments = pipedArguments;
    }

	public String getId() {
    	return id;
    }

	public Map<String, String> getPipedArguments() {
    	return pipedArguments;
    }
	
	public String toString() {
		return id + "=" + pipedArguments;
	}
}
