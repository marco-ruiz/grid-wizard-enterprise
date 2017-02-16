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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Ruiz
 * @since Aug 19, 2008
 */
public class PVarValue<VALUE_TYPE extends Serializable> implements Serializable {
	
	protected VALUE_TYPE value;
	protected transient Map<PProcessorType, List<PProcessor>> processors = new HashMap<PProcessorType, List<PProcessor>>();
	
	public PVarValue(VALUE_TYPE value) {
		this(value, null, null);
    }
	
	public PVarValue(VALUE_TYPE value, PProcessor preProcessor, PProcessor postProcessor) {
	    for (PProcessorType procType : PProcessorType.values()) processors.put(procType, new ArrayList<PProcessor>());

	    this.value = value;
	    
	    if (preProcessor != null)  getProcessors(PProcessorType.PRE).add(preProcessor);
	    if (postProcessor != null) getProcessors(PProcessorType.POST).add(postProcessor);
    }
	
	public Object getVTLModel() {
    	return value;
    }
	
	public List<PProcessor> getProcessors(PProcessorType procType) {
		if (processors == null) processors = new HashMap<PProcessorType, List<PProcessor>>();
    	List<PProcessor> result = processors.get(procType);
    	if (result == null) {
    		result = new ArrayList<PProcessor>();
    		processors.put(procType, result);
    	}
		return result;
    }
	
	public String toString() {
		return value.toString();
	}
	
	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((value == null) ? 0 : value.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    final PVarValue other = (PVarValue) obj;
	    if (value == null) {
		    if (other.value != null)
			    return false;
	    } else if (!value.equals(other.value))
		    return false;
	    return true;
    }
}

