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

package org.gwe.p2elv2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Marco Ruiz
 * @since Jul 30, 2008
 */
public abstract class PFunction {
	
	private static Map<String, PFunction> functionsRegistry = new HashMap<String, PFunction>();
	public static PFunction getFunction(String functionName) { return functionsRegistry.get(functionName); }
	
	private String name;
	
	public PFunction(String name) {
	    this.name = name;
	    functionsRegistry.put(name, this);
    }

	public String getName() {
    	return name;
    }
	
	public boolean isSingleValue(List<String> params) {
		return false;
	}

	public boolean isRuntime() {
	    return false;
    }

	public boolean isCompiletime() {
	    return !isRuntime();
    }

	public abstract PVarValueSpace calculateValues(List<String> params, PStatementContext ctx);
}

