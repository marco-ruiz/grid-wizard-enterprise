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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.P2ELFunctionNotSupported;
import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValueSpace;
import org.gwe.p2elv2.functions.PFConst;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Jul 30, 2008
 */
@REXConfig4Class(rexPieces={"\\s*\\${1,2}\\{\\s*", "nameParts", "\\}\\s*=", "functionInvocation|constantValue", "\\s*"})
public class PVariable implements Serializable {
	
	public static final String SYS_VAR_PREFIX = "SYSTEM";
	public static final String RUN_VAR_PREFIX = "RUNTIME";
	
	@REXConfig4ListElement(pattern=@REXConfig4Field(field="[_a-zA-Z]\\w*", suffix="[.]?"), min=1)
	private List<String> nameParts = null;

	private PFunctionInvocation functionInvocation = null;

	@REXConfig4String(pattern=@REXConfig4Field(field="[^\\s]*"))
	private String constantValue = null;
	
	protected String name = "";
	private String dimension = "";
	
	public PVariable() {}
	
	public PVariable(String name, String constantValue) {
		setNameParts(name);
		setConstantValue(constantValue);
	}
	
	public void setNameParts(String... nameParts) {
		setNameParts(new ArrayList<String>(Arrays.asList(nameParts)));
	}
	
	public void setNameParts(List<String> nameParts) {
		if (nameParts.isEmpty()) return;
		name = nameParts.remove(0);
		if (nameParts.isEmpty()) return;
		dimension = nameParts.remove(0);
		for (String part : nameParts) dimension += "." + part;
    }

	public String getName() {
		return name;
    }

	public String getDimension() {
		return dimension;
    }
	
	public PFunctionInvocation getFunctionInvocation() {
		return functionInvocation; 
	}
	
	public void setFunctionInvocation(PFunctionInvocation functionInvocation) {
		this.functionInvocation = functionInvocation; 
	}
	
	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
		// In "const" function form
		setFunctionInvocation(PFunctionInvocation.create(PFConst.FUNCTION_NAME, constantValue));
    }

	public boolean isSingleValue() throws P2ELFunctionNotSupported {
	    return getFunction().isSingleValue(functionInvocation.getParams());
    }

	public boolean isRuntime() throws P2ELFunctionNotSupported {
	    return getFunction().isRuntime();
    }

	private PFunction getFunction() throws P2ELFunctionNotSupported {
	    PFunction result = functionInvocation.getFunction();
	    if (result == null) throw new P2ELFunctionNotSupported(functionInvocation.getFunctionName());
	    return result;
    }

	public boolean isVarConstSingleValue() {
	    try {
	        return isSingleValue() && getFunctionInvocation().getFunction().getClass().equals(PFConst.class);
        } catch (P2ELFunctionNotSupported e) {
        	return false;
        }
    }
	
	public boolean isPermutable() {
	    try {
	        return !isSingleValue() && !isRuntime() && !isDependentOnSystemVars() &&  !isDependentOnRuntimeVars();
        } catch (P2ELFunctionNotSupported e) {
        	return false;
        }
    }

	public PVarValueSpace generateValueSpace(PPermutation permKey, PStatementContext ctx) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
	    try {
	    	List<String> evalParams = functionInvocation.evalParams(permKey.asVTLModel());
		    return getFunction().calculateValues(evalParams, ctx);
	    } catch (P2ELDependentVariableNotResolvedException e) {
	    	e.setVariable(this);
	    	throw e;
	    }
    }

	public String getFullName() {
	    return name + (dimension.equals("") ? "" : "." + dimension);
    }

	public String getVarReference() {
	    return "${" + getFullName() + "}";
    }

	public String toString() {
		String value = (constantValue != null) ? constantValue : functionInvocation.toString(); 
		return getVarReference() + "=" + value; 
	}
	
	//========================
	// DEPENDENCIES UTILITIES
	//========================

	public boolean isVarResolvedBy(List<PVariable> varsResolved, String... varsResolvedGroups) {
	    Set<String> varDependencyNames = getVarDependencyNames();
	    for (String dep : varDependencyNames) 
        	if (!isDependencyResolvedByGroups(dep, varsResolvedGroups) && !isDependencyResolvedByVars(dep, varsResolved))
        		return false;

	    return true;
    }
	
	private boolean isDependencyResolvedByGroups(String dependency, String... varsResolvedGroups) {
    	for (String group : varsResolvedGroups) 
            if (dependency.startsWith(group)) return true;
    	
    	return false;
	}
	
	private boolean isDependencyResolvedByVars(String dependency, List<PVariable> varsResolved) {
        for (PVariable var : varsResolved) 
            if (var.getName().equals(dependency)) return true;
    	
    	return false;
	}
	
	public int getDependencyOrder(List<PVariable> varsSortedByDependencyLevel) {
	    Set<String> varDependencyNames = getVarDependencyNames();
	    int idx = varsSortedByDependencyLevel.size();
	    for (; idx > 0; idx--) {
			PVariable sortedVar = varsSortedByDependencyLevel.get(idx - 1);
			if (varDependencyNames.contains(sortedVar.getName())) 
	        	break;
	    }
	    return idx;
    }
	
	public boolean isDependentOnSystemVars() {
		for (String varName : getVarDependencyNames()) 
	        if (varName.startsWith(SYS_VAR_PREFIX)) 
	        	return true;
		
		return false;
	}
	
	public boolean isDependentOnRuntimeVars() {
		for (String varName : getVarDependencyNames()) 
	        if (varName.startsWith(RUN_VAR_PREFIX)) 
	        	return true;
		
		return false;
	}
	
	public Set<String> getVarDependencyNames() {
	    return functionInvocation.getVarDependencyNames();
    }
}

