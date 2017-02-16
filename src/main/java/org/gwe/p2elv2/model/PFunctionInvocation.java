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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.PFunction;
import org.gwe.utils.VelocityUtils;
import org.gwe.utils.rex.REXParser;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Jul 30, 2008
 */
@REXConfig4Class(rexPieces={"\\$", "functionName", "\\s*\\(", "params", "\\)"})
public class PFunctionInvocation implements Serializable {
	
	public static PFunctionInvocation create(String functionName, String... values) {
	    PFunctionInvocation invocation = new PFunctionInvocation();
		invocation.setFunctionName(functionName);
		List<String> params = new ArrayList<String>();
		for (String val : values) params.add(val);
		invocation.setParams(params);
		return invocation;
    }

	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	private String functionName;
	
	@REXConfig4ListElement(pattern=@REXConfig4Field(field="[^\\(\\),]*", suffix="[,]?"), min=1)
	private List<String> params;
	
	private Set<String> varDependencyNames = new HashSet<String>();
	
	public String getFunctionName() { 
		return functionName; 
	}
	
	public void setFunctionName(String functionName) { 
		this.functionName = functionName; 
	}
	
	public PFunction getFunction() { 
		return PFunction.getFunction(functionName); 
	}
	
	public List<String> getParams() { 
		return params; 
	}
	
	public void setParams(List<String> params) { 
		this.params = params;
		
		// Resolve params variable references
        for (String param : params) {
        	try {
        		for (PVarRef varRef : REXParser.createModel(PVarReferences.class, param).getVarRefs())
        			varDependencyNames.add(varRef.getName());
            } catch (Exception e) {
            }
        } 
	}
	
    public Set<String> getVarDependencyNames() {
    	return varDependencyNames;
    }

	public List<String> evalParams(Map<String, Object> vtlModel) throws P2ELDependentVariableNotResolvedException {
	    // Verify dependencies resolution
		Set<String> dependencies = new HashSet<String>(varDependencyNames);
        for (String varName : vtlModel.keySet()) 
        	dependencies.remove(varName);
        
        if (!dependencies.isEmpty()) 
        	throw new P2ELDependentVariableNotResolvedException(dependencies);
		
		// Evaluate parameters
		List<String> evaluatedParams = new ArrayList<String>();
        for (String param : params) {
    		String evalParam = (vtlModel.isEmpty()) ? param : VelocityUtils.merge(vtlModel, param);
			evaluatedParams.add(evalParam);
        }
	    return evaluatedParams;
    }
	
	public String toString() { 
		String paramsStr = "";
		for (String param : params) {
			if (!paramsStr.equals("")) paramsStr += ",";
            paramsStr += param;
        }

		return "$" + functionName + "(" + paramsStr + ")"; 
	}
}

