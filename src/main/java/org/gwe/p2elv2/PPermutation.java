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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.gwe.p2elv2.model.PVariable;
import org.gwe.p2elv2.model.PVariableArray;
import org.gwe.utils.VelocityUtils;

/**
 * @author Marco Ruiz
 * @since Jul 31, 2008
 */
public class PPermutation extends HashMap<PVariable, PVarValue> {
	
	private List<PVariable> pendingSortedVars = new ArrayList<PVariable>();

	public PPermutation() {}
	
	public void addPendingVar(PVariable var) {
		pendingSortedVars.add(var);
    }
	
	public PPermutation clone() {
	    PPermutation result = new PPermutation();
	    result.putAll(this);
	    result.pendingSortedVars.addAll(this.pendingSortedVars);
	    return result;
    }

	public PPermutation cloneAddingVar(PVariable var, PVarValue<?> value) {
	    PPermutation clone = new PPermutation();
	    clone.putAll(this);
		clone.put(var, value);
	    return clone;
    }
	
	public PPermutation cloneOnlyWithVars(Set<String> varNames) {
	    PPermutation clone = new PPermutation();
		for (Map.Entry<PVariable, PVarValue> entry : this.entrySet()) {
			String varName = entry.getKey().getName();
			if (varNames.contains(varName)) 
				clone.put(entry.getKey(), entry.getValue());
        }
		
	    return clone;
    }

	public void processSystemDependencies(PStatementContext ctx) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
		addVars(ctx.getSystemVars());

		Set<PVariable> processedVars = new HashSet<PVariable>();
		for (PVariable var : pendingSortedVars) {
			if (var.isRuntime() || var.isDependentOnRuntimeVars()) break;
			put(var, var.generateValueSpace(this, ctx).get(0));
		}
		pendingSortedVars.removeAll(processedVars);
	}

	public void processRuntimedependencies(PStatementContext ctx) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
		addVars(ctx.getRuntimeVars());
		
		for (PVariable var : pendingSortedVars) 
			put(var, var.generateValueSpace(this, ctx).get(0));
		pendingSortedVars.clear();
	}

	private void addVars(Map<String, String> vars) {
	    for (Map.Entry<String, String> var : vars.entrySet())
	        put(new PVariable(var.getKey(), var.getValue()), new PVarValue<String>(var.getValue()));
    }

	public List<PProcessor> getProcessors(PProcessorType procType) {
		List<PProcessor> result = new ArrayList<PProcessor>();
		for (PVarValue value : values())
	        result.addAll(value.getProcessors(procType));
		return result;
	}
	
	//====================
	// PRESENTATION LAYER
	//====================
	public String merge(String template) {
    	return VelocityUtils.merge(asVTLModel(), template);
	}
	
	public Map<String, Object> asVTLModel() {
	    Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry<PVariable, PVarValue> entry : this.entrySet())
			result.put(entry.getKey().getName(), entry.getValue().getVTLModel());
	    return result;
    }

	public List<String> getEntriesStr() {
		List<String> result = new ArrayList<String>();
		for (Map.Entry<PVariable, PVarValue> entry : getSortedCopy().entrySet())
	        result.add(entry.getKey().getFullName() + "=" + entry.getValue());
		return result;
	}

	private Map<PVariable, PVarValue> getSortedCopy() {
	    Map<PVariable, PVarValue> sorted = new TreeMap<PVariable, PVarValue>(new Comparator<PVariable>() {
			public int compare(PVariable var1, PVariable var2) {
	            return var1.getFullName().compareTo(var2.getFullName());
            }
		});
	    sorted.putAll(this);
	    return sorted;
    }
	
	public TreeMap<String, Object> asFriendlyTreeMap() {
	    TreeMap<String, Object> result = new TreeMap<String, Object>();
	    for (Map.Entry<PVariable, PVarValue> entry : entrySet()) {
	        PVariable var = entry.getKey();
	        PVarValue varValue = entry.getValue();
	        Object value = varValue.getVTLModel();
			if (value instanceof Map && var instanceof PVariableArray) {
				for (PVariable varChild : ((PVariableArray)var).getVars())
					result.put(varChild.getFullName(), ((Map)value).get(varChild.getDimension()));
			} else 
				result.put(var.getFullName(), value);
        }
	    return result;
    }

	public String toString() {
		String result = "(";
		for (String entryStr : getEntriesStr()) {
			if (!result.equals("(")) result += ",";
	        result += entryStr;
        }
		return result + ")";
	}
}

