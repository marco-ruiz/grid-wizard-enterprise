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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.P2ELFunctionNotSupported;
import org.gwe.utils.VelocityUtils;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Jul 28, 2008
 */
@REXConfig4Class(rexPieces={"vars", "template"})
public class PStatement {
	
	@REXConfig4ListElement(min=0)
	private List<PVariable> vars;
	
	@REXConfig4String(pattern=@REXConfig4Field(field=".*"))
	private String template;

	public List<PVariable> getVars() { 
		return vars; 
	}
	
	public void setVars(List<PVariable> vars) { 
		this.vars = vars; 
	}
	
	public String getTemplate() { 
		return template; 
	}
	
	public void setTemplate(String template) { 
		this.template = template; 
	}
	
	public void inlineConstants() {
		List<PVariable> inlineVars = new ArrayList<PVariable>();
		for (PVariable var : vars) 
			if (var.isVarConstSingleValue()) inlineVars.add(var);
		
		for (PVariable inlineVar : inlineVars) {
			Map<String, Object> constantModel = new HashMap<String, Object>();
			List<String> params = inlineVar.getFunctionInvocation().getParams();
			String constVal = params.size() > 0 ? params.get(0) : "";
			constantModel.put(inlineVar.getName(), constVal);
			setTemplate(VelocityUtils.merge(constantModel, getTemplate()));
			
			for (PVariable var : vars) {
				List<String> inlinedParams = new ArrayList<String>();
				for (String param : var.getFunctionInvocation().getParams())
					inlinedParams.add(VelocityUtils.merge(constantModel, param));
				var.getFunctionInvocation().setParams(inlinedParams);
	        }
        }
		
		vars.removeAll(inlineVars);
	}
	
	public List<PVariable> getVarsSortedByDependencies() throws P2ELFunctionNotSupported, P2ELDependentVariableNotResolvedException {
	    List<PVariable> nonRuntimeVars = new ArrayList<PVariable>();
	    List<PVariable> runtimeVars = new ArrayList<PVariable>();
	    for (PVariable var : groupVariables()) {
	    	boolean varIsRuntime = var.isRuntime() || var.isDependentOnSystemVars() || var.isDependentOnRuntimeVars();
			List<PVariable> dest = varIsRuntime ? runtimeVars : nonRuntimeVars;
	    	dest.add(var);
        }
	    
	    List<PVariable> result = new ArrayList<PVariable>();

	    // Sort as many non-runtime variables as possible
	    try {
	    	sortVars(nonRuntimeVars, result);
	    } catch(P2ELDependentVariableNotResolvedException e) {
		    // If some left over make sure non of them are multi-value (expand to multiple process invocations)
	    	// In case they all are just proceed since they could be resolved later with runtime single values.
	    	for (PVariable unresolvedVar : nonRuntimeVars)
	            if (!unresolvedVar.isSingleValue()) throw e;
		    runtimeVars.addAll(nonRuntimeVars);
	    }

	    // Sort remaining runtime variables
	    try {
	        sortVars(runtimeVars, result, PVariable.SYS_VAR_PREFIX);
	    } catch(P2ELDependentVariableNotResolvedException e) {}
	    sortVars(runtimeVars, result, PVariable.RUN_VAR_PREFIX);
        
        return result;
    }

/*
	private void sortVars(List<PVariable> runtimeVars, List<PVariable> result) {
	    for (PVariable var : runtimeVars) { 
            int sortedIndex = var.getDependencyOrder(result);
			result.add(sortedIndex, var);
        }
    }
*/

	private void sortVars(List<PVariable> unresolved, List<PVariable> resolved, String... resolvedGroups) throws P2ELDependentVariableNotResolvedException {
		while (!unresolved.isEmpty()) {
			boolean resolvedVar = false;
			for (int idx = 0; idx < unresolved.size(); idx++) {
				if (unresolved.get(idx).isVarResolvedBy(resolved, resolvedGroups)) {
					resolved.add(unresolved.remove(idx));
					resolvedVar = true;
					break;
				}
            }
			if (!resolvedVar) {
	            PVariable var = unresolved.get(0);
	            throw new P2ELDependentVariableNotResolvedException(var.getVarDependencyNames(), var);
            }
		}
    }
	
	private Collection<PVariableArray> groupVariables() {
		Map<String, PVariableArray> arrayVars = new HashMap<String, PVariableArray>();
		for (PVariable var : vars) {
			String varName = var.getName();
			PVariableArray parentArray = arrayVars.get(varName);
			if (parentArray == null) {
				parentArray = new PVariableArray(varName);
				arrayVars.put(varName, parentArray);
			}
			parentArray.addVar(var);
        }

		// Add all grouped variables 
	    return arrayVars.values();
    }

	public String toString() { 
		return toStringFormatted("", " ");
	}

	public String toStringFormatted() {
		return toStringFormatted("\t", "\n");
    }

	public String toStringFormatted(String prefix, String suffix) {
		String varsStr = "";
		for (PVariable var : vars) varsStr += prefix + var + suffix;
		return varsStr + suffix + prefix + template; 
    }
}

