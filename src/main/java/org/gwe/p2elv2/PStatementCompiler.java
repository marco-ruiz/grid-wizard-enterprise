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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gwe.drivers.fileSystems.staging.FilesStager;
import org.gwe.p2elv2.model.PStatement;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since Aug 6, 2008
 */
public class PStatementCompiler {
	
	private PStatement stmt;
	private PStatementContext ctx;
	
	private Map<PVariable, Map<PPermutation, PVarValueSpace>> cachedValueSpaceSet = 
		new HashMap<PVariable, Map<PPermutation, PVarValueSpace>>();

	private List<PVariable> varsSorted;
	
	public PStatementCompiler(PStatement stmt, String workspace, KeyStore keys, FilesStager stager) throws P2ELFunctionNotSupported, P2ELDependentVariableNotResolvedException {
		this(stmt, new PStatementContext(workspace, keys, stager));
    }

	public PStatementCompiler(PStatement stmt, PStatementContext ctx) throws P2ELFunctionNotSupported, P2ELDependentVariableNotResolvedException {
		setStmt(stmt);
		this.ctx = ctx;
    }

	public PStatement getStmt() {
    	return stmt;
    }

	public void setStmt(PStatement stmt) throws P2ELFunctionNotSupported, P2ELDependentVariableNotResolvedException {
    	this.stmt = stmt;
    	this.varsSorted = stmt.getVarsSortedByDependencies();
        for (PVariable var : varsSorted)
        	cachedValueSpaceSet.put(var, new HashMap<PPermutation, PVarValueSpace>());
    }

	public List<PPermutation> compile() throws P2ELDependentVariableNotResolvedException, P2ELMultiValueVarDependentOnRuntimeVarException, P2ELFunctionNotSupported {
		List<PPermutation> initialPermutations = new ArrayList<PPermutation>();
		initialPermutations.add(new PPermutation());

		Set<PVariable> varsPermutated = initialPermutations.get(0).keySet();
		
		// Skip the already compiled vars
		int index = 0;
        for (; index < varsSorted.size(); index++) {
    		if (!varsPermutated.contains(varsSorted.get(index)))
    			break;
        }
        
		// Compile the remaining vars up to the runtime ones (if precompiling)
        for (; index < varsSorted.size(); index++) {
        	PVariable var = varsSorted.get(index);
			if (var.isRuntime() || var.isDependentOnSystemVars()) 
				break;
        	initialPermutations = permutateWithVar(var, initialPermutations);
        }
        
		// Store remaining 'un-compiled' vars in each permutation as vars pending compilation
        for (; index < varsSorted.size(); index++) {
        	PVariable var = varsSorted.get(index);
    		if (!var.isSingleValue()) throw new P2ELMultiValueVarDependentOnRuntimeVarException(var);
        	for (PPermutation perm : initialPermutations) perm.addPendingVar(var);
        }
        
        return initialPermutations;
	}
	
	private List<PPermutation> permutateWithVar(PVariable var, List<PPermutation> parentPermutations) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
	    List<PPermutation> currPerms = new ArrayList<PPermutation>();
	    for (PPermutation parentPerm : parentPermutations)
            for (PVarValue value : getValueSpace(var, parentPerm)) 
            	currPerms.add(parentPerm.cloneAddingVar(var, value));

	    return currPerms;
    }
	
	private PVarValueSpace getValueSpace(PVariable var, PPermutation parentPerm) throws P2ELDependentVariableNotResolvedException, P2ELFunctionNotSupported {
	    PPermutation permKey = parentPerm.cloneOnlyWithVars(var.getVarDependencyNames());
	    Map<PPermutation, PVarValueSpace> varValueSpaceCache = cachedValueSpaceSet.get(var);
	    PVarValueSpace valueSpace = varValueSpaceCache.get(permKey);
	    if (valueSpace == null) {
	        valueSpace = var.generateValueSpace(permKey, ctx);
	    	varValueSpaceCache.put(permKey, valueSpace);
	    }
	    return valueSpace;
    }

	public List<String> createStatements(List<PPermutation> permutations) throws P2ELDependentVariableNotResolvedException, P2ELMultiValueVarDependentOnRuntimeVarException {
		List<String> result = new ArrayList<String>();
		for (PPermutation perm : permutations) result.add(perm.merge(stmt.getTemplate()) + "\n");
		return result;
	}
}

