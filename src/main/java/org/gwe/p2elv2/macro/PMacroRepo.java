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

package org.gwe.p2elv2.macro;

import java.util.ArrayList;
import java.util.List;

import org.gwe.p2elv2.model.PStatement;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.rex.REXParser;

/**
 * @author Marco Ruiz
 * @since Feb 23, 2009
 */
public class PMacroRepo extends ArrayList<PMacroLibrary> {
	
	public String applyMacros(String stmtStr) throws REXException {
		PStatement stmt = REXParser.createModel(PStatement.class, stmtStr);
		List<PVariable> stmtMacroVars = new ArrayList<PVariable>(); 
		List<PVariable> macroExpandedVars = new ArrayList<PVariable>(); 
		
		for (PVariable var : stmt.getVars()) {
        	List<PVariable> macroVars = expandMacro(var);
	        if (macroVars != null) {
	        	stmtMacroVars.add(var);
				macroExpandedVars.addAll(macroVars);
	        }
        }
		stmt.getVars().removeAll(stmtMacroVars);
		stmt.getVars().addAll(macroExpandedVars);
		String result = stmt.toString();
		return (stmtMacroVars.isEmpty()) ? result : applyMacros(result);
	}

	private List<PVariable> expandMacro(PVariable var) throws REXException {
		PMacro macroObj = null;
		for (PMacroLibrary macroLib : this) {
	        macroObj = macroLib.getInvocationMacro(var);
	        if (macroObj != null) break;
        }

        if (macroObj == null) return null;
	    String evalContent = macroObj.evaluateContent(var.getFullName(), var.getFunctionInvocation().getParams());
	    return REXParser.createModel(PStatement.class, evalContent).getVars();
    }
}
