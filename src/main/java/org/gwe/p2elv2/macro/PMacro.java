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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.rex.REXParser;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Dec 5, 2008
 */
@REXConfig4Class(rexPieces={"documentation", "\\s*\\$", "macroName", "\\s*\\(", "params", "\\)\\s*\\{\\s*", "vars", "\\s*}\\s*"})
public class PMacro {

	private static final String TAG_TITLE = "TITLE";
	private static final String TAG_DESCRIPTION = "DESCRIPTION";

	private Map<String, String> structureDocumentation = new HashMap<String, String>();

	@REXConfig4ListElement(min=0)
	private List<PDocumentationLine> documentation;
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	private String macroName;
	
	@REXConfig4ListElement(min=0)
	private List<PMacroParam> params;
	
	@REXConfig4ListElement(min=0)
	private List<PVariable> vars;

	@REXConfig4String(pattern=@REXConfig4Field(field=".*?", suffix="\\s*}"))
	private String content;
	
	private String category;
	
	public void documentComponents() {}
	
	public String getTitle() {
    	return structureDocumentation.get(TAG_TITLE);
    }

	public String getDescription() {
    	return structureDocumentation.get(TAG_DESCRIPTION);
    }

	public String getDescription(PVariable var) {
    	return structureDocumentation.get(var.getFullName());
    }

	public void setDocumentation(List<PDocumentationLine> description) {
    	this.documentation = description;
    	structureDocumentation();
    }

	private void structureDocumentation() {
	    structureDocumentation.put(TAG_DESCRIPTION, "Not Available.");
    	String lastTag = null;
    	for (PDocumentationLine line : documentation) {
    		lastTag = resolveLastTag(line, lastTag);
    		if (lastTag != null) {
    			String prefix = structureDocumentation.get(lastTag);
    			structureDocumentation.put(lastTag, prefix + " " + line.getContent());
    		}
        }
    }

	private String resolveLastTag(PDocumentationLine line, String lastToken) {
	    String lineTag = line.getTag();
	    if (lineTag != null && !lineTag.equals("")) {
	    	lastToken = lineTag;
	    	structureDocumentation.put(lastToken, "");
	    }
	    return lastToken;
    }

	public String getMacroName() {
    	return macroName;
    }

	public void setMacroName(String macroName) {
    	this.macroName = macroName;
    }

	public List<PMacroParam> getParams() {
    	return params;
    }

	public void setParams(List<PMacroParam> params) {
    	this.params = params;
    }
	
	public List<PVariable> getVars() {
    	return vars;
    }

	public void setVars(List<PVariable> vars) {
    	this.vars = vars;
    	this.content = "";
    	// The extra '$' sign is to make all variables relative to this macro
    	for (PVariable var : vars) this.content += "$" + var + " "; 
    }

	public String getContent() {
    	return content;
    }

	public void setContent(String content) {
		if (content.endsWith("}")) content = content.substring(0, content.length() - 1);
    	this.content = content;
    }

	public void setCategory(String category) {
		this.category = category;
    }

	public String evaluateContent(String preffix, List<String> args) {
		return contextualize(preffix, substituteParameters(args));
	}

	private String substituteParameters(List<String> args) {
	    String resultContent = content;
		for (int idx = 0; idx < params.size(); idx++) { 
			String value = (idx < args.size()) ? args.get(idx) : "";
			resultContent = resultContent.replace(params.get(idx).asParam(), value);
		}
		
	    return resultContent;
    }

	private String contextualize(String preffix, String params) {
		params = params.replaceAll("\\$\\$\\{_}", "\\$\\{" + preffix + "}");
	    return params.replaceAll("\\$\\$\\{", "\\$\\{" + preffix + "_");
    }
	
	public String toString() {
		return content;
	}

	public static void main(String[] argss) throws Exception {
		String macro = 
"		$slicer($${SLICER_MODULE},$${FIXED_PATH},$${MOVING_PATH},$${OUTPUT_PATH},$${SLICER_HOME}) {"+
"			$${CMD}=$const($${SLICER_HOME}/Slicer3 --launch $${SLICER_HOME}/lib/Slicer3/Plugins/$${SLICER_MODULE})"+
"			$${FIXED}=$in($${FIXED_PATH}/fixed.nrrd?view=co,fixed.nrrd) "+
"			$${MOVING}=$in($${MOVING_PATH}/moving.nrrd?view=co,moving.nrrd)"+
"			$${OUTPUT}=$out($${OUTPUT_PATH}/out-${SYSTEM.JOB_NUM}.nrrd) "+
"			$${_}=$const(value) "+
"		}";
		
		PMacro result = REXParser.createModel(PMacro.class, macro, true);
		System.out.println(result);
		
		List<String> args = new ArrayList<String>();
		args.add("${MODULE}");
		args.add("/export/fix");
		args.add("/export/mov");
		args.add("/export/out");
		args.add("/apps/Slicer");
		
		String res = result.evaluateContent("BSPLINE", args);
		System.out.println(res);
	}
}

