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

package org.gwe.p2elv2.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.P2ELFunctionNotSupported;
import org.gwe.p2elv2.PArrayVarValue;
import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;
import org.gwe.p2elv2.PVarValueSpace;

/**
 * @author Marco Ruiz
 * @since Aug 14, 2008
 */
public class PVariableArray extends PVariable {
	
	private List<PVariable> vars = new ArrayList<PVariable>();
	private Set<String> varDependencies = new HashSet<String>();
	
	public PVariableArray(String name) {
		this.name = name;
	}
	
	public List<PVariable> getVars() {
    	return vars;
    }

	public void addVar(PVariable var) {
		vars.add(var);
		varDependencies.addAll(var.getVarDependencyNames());
	}
	
	public PVarValueSpace generateValueSpace(PPermutation permKey, PStatementContext ctx) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
		PArrayVarValues values = new PArrayVarValues();
		for (PVariable var : vars) 
			values.put(var.getDimension(), var.generateValueSpace(permKey, ctx));
        return values.getDimensionalValues();
    }

	public int getDependencyOrder(List<PVariable> varsSortedByDependencyLevel) {
		int maxIndex = 0;
		int currentIndex = 0;
		for (PVariable var : vars) {
			currentIndex = var.getDependencyOrder(varsSortedByDependencyLevel);
			if (maxIndex < currentIndex) maxIndex = currentIndex;
        }
		return maxIndex;
    }

    public Set<String> getVarDependencyNames() {
	    return varDependencies;
    }

	public boolean isSingleValue() throws P2ELFunctionNotSupported {
		for (PVariable var : vars) 
			if (!var.isSingleValue()) return false;
		
		return true;
	}
	
	public boolean isRuntime() throws P2ELFunctionNotSupported {
		for (PVariable var : vars) 
			if (var.isRuntime()) return true;
		
		return false;
    }

	public String toString() {
		return vars.toString();
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    result = prime * result + ((vars == null) ? 0 : vars.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (!super.equals(obj))
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    final PVariableArray other = (PVariableArray) obj;
	    if (vars == null) {
		    if (other.vars != null)
			    return false;
	    } else if (!vars.equals(other.vars))
		    return false;
	    return true;
    }
	
	class PArrayVarValues extends HashMap<String, PVarValueSpace> {

		public PVarValueSpace getDimensionalValues() {
			PVarValueSpace result = new PVarValueSpace();
	        PVarValue nextDimValues;
	        while ((nextDimValues = getNextDimensionalValues()) != null) 
	        	result.add(nextDimValues);
	        
			return result;
	    }
		
		private PVarValue getNextDimensionalValues() {
	        boolean foundValues = false;
	        HashMap<String, PVarValue> valueMap = new HashMap<String, PVarValue>();
		    for (Map.Entry<String, PVarValueSpace> dimValues : entrySet()) {
		    	PVarValueSpace valList = dimValues.getValue();
		    	PVarValue val = new PVarValue("");
		    	if (!valList.isEmpty()) {
		    		foundValues = true;
		    		val = (PVarValue) valList.remove(0);
		    	}
		    	valueMap.put(dimValues.getKey(), val);
		    }
		    return foundValues ? new PArrayVarValue(valueMap) : null;
	    }
	}
}

