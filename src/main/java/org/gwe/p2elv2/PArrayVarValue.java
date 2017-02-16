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

/**
 * @author Marco Ruiz
 * @since Aug 19, 2008
 */
public class PArrayVarValue extends PVarValue<HashMap<String, PVarValue>> {
	
	public PArrayVarValue(HashMap<String, PVarValue> value) {
		super(value);
		for (PVarValue<?> varValue : value.values()) { 
			addAllProcessors(varValue, PProcessorType.PRE);
			addAllProcessors(varValue, PProcessorType.POST);
		}
	}
	
	public void addDimensionalValue(String dimension, String dimValue) {
		addDimensionalValue(dimension, new PVarValue(dimValue));
	}

	public void addDimensionalValue(String dimension, PVarValue<?> dimValue) {
		value.put(dimension, dimValue);
	}

	private void addAllProcessors(PVarValue<?> varValue, PProcessorType procType) {
	    getProcessors(procType).addAll(varValue.getProcessors(procType));
    }
	
	public Object getVTLModel() {
		Object rootValue = value.get("");
		return (value.size() == 1 && rootValue != null) ? rootValue : value;
	}
}

