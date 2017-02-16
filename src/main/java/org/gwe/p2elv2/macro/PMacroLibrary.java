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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.IOUtils;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.rex.REXParser;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Dec 5, 2008
 */
@REXConfig4Class(rexPieces={"\\s*", "macros", "\\s*"})
public class PMacroLibrary {
	
	public static PMacroLibrary read(String macroLib, boolean fileName) throws REXException, FileNotFoundException, IOException {
		if (fileName) macroLib = new String(IOUtils.readFile(macroLib));
	    return REXParser.createModel(PMacroLibrary.class, macroLib, false);
    }
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	private String category;

	@REXConfig4ListElement(min=0)
	private List<PMacro> macros;

	private Map<String, PMacro> repo = new HashMap<String, PMacro>();
	
	public String getCategory() {
    	return category;
    }

	public void setCategory(String category) {
    	this.category = category;
    }

	public void addMacros(PMacroLibrary otherLib) {
		addMacros(otherLib.macros);
	}

	public void setMacros(List<PMacro> macros) {
		this.macros = null;
    	addMacros(macros);
    }

	public List<PMacro> getMacros() {
    	return new ArrayList<PMacro>(macros);
    }

	private void addMacros(List<PMacro> macros) {
		if (this.macros == null) this.macros = new ArrayList<PMacro>(); 
	    this.macros.addAll(macros);
    	for (PMacro macro : macros) {
    		macro.setCategory(category);
    		repo.put(macro.getMacroName(), macro);
    	}
    }

	public PMacro getInvocationMacro(PVariable var) {
	    return repo.get(var.getFunctionInvocation().getFunctionName());
    }
	
	public static void main(String[] args) throws REXException, IOException {
		String macroLib = 
			"		$slicer($${SLICER_MODULE},$${FIXED_PATH},$${MOVING_PATH},$${OUTPUT_PATH},$${SLICER_HOME}) {"+
			"			$${CMD}=$const($${SLICER_HOME}/Slicer3 --launch $${SLICER_HOME}/lib/Slicer3/Plugins/$${SLICER_MODULE})"+
			"			$${FIXED}=$in($${FIXED_PATH}/fixed.nrrd?view=co,fixed.nrrd) "+
			"			$${MOVING}=$in($${MOVING_PATH}/moving.nrrd?view=co,moving.nrrd)"+
			"			$${OUTPUT}=$out($${OUTPUT_PATH}/out-${SYSTEM.JOB_NUM}.nrrd) "+
			"		}";
				
		PMacroLibrary lib = PMacroLibrary.read("/Users/admin/work/eclipse-ws/gwe-core/src/main/config/base/gwe-p2el.macro", true);
				
		String stmtStr = "${SLICER}=$slicer(BSplineDeformableRegistration,${FILES_DIR},${FILES_DIR},sftp://destinationHost/path,/apps/Slicer) ${SLICER_CMD} --resampledmovingfilename ${SLICER.OUTPUT} ${SLICER.FIXED} ${SLICER.MOVING}";
		
		stmtStr = "    ${SCAN}=$range(1,4)"+
			"	${MOV_RANGE}=$range(1,51,10)"+
			"	${FIX}=$oasis(0101,${SCAN})"+
			"	${MOV}=$oasis(${MOV_RANGE},${SCAN})"+
			"    ${ITER}=$const(10,20) ${HIST}=$range(20,100,060) ${SAM}=$range(500,5000,3000)"+
			"    ${BSPLINE}=$bspline(${SLICER_HOME},${ITER},${HIST},${SAM})"+
			"	${OUT}=$uploadHDR(${SYSTEM.USER_HOME}/oasis-results/${BSPLINE_OUT_DIR}/out-${SYSTEM.JOB_NUM}-${MOV_NUM})"+
			"	${BSPLINE_CMD} ${OUT_HDR} ${FIX_HDR} ${MOV_HDR}";

		PMacroRepo macroRepo = new PMacroRepo();
		macroRepo.add(lib);
		System.out.println(macroRepo.applyMacros(stmtStr));
	}
}
