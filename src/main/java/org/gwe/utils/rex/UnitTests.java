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

package org.gwe.utils.rex;

import java.util.List;

import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4ListElement;
import org.gwe.utils.rex.config.REXConfig4String;
import org.gwe.utils.rex.config.REXInspector4Class;

/**
 * @author Marco Ruiz
 * @since Jul 23, 2008
 */
class UnitTests {
	public static void main(String[] args) throws Exception {
		System.out.println(REXInspector4Class.getConfig(Statement.class).getPattern());
		Statement stmt = new Statement();
		String p2el = "${myArray(1,2,3)}=[(a,b,c),(4,5,6),(7,8,9)] ${FILE}=f:expand(file, path, now) ${HIST}=[20..100||010] ${Array01(1,2,3)}=[(1,2,3),(4,5,6),(7,8,9)] ${function(x,y)}=f:myfunction(params) ${SAM}=[500..5000||0500] ${ITER}=[10..50||10] /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co";
		REXParser.populateModel(stmt, p2el, false);
//		REXParser.populateModel(stmt, "${function(x,y)}=f:myfunction(params) ${ITER}=f:expand(file, path, now) /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/Slicer3 --launch /home/mruiz/Slicer3-3.0.2008-01-05-linux-x86/lib/Slicer3/Plugins/BSplineDeformableRegistration --iterations ${ITER} --gridSize 5 --histogrambins ${HIST} --spatialsamples ${SAM} --maximumDeformation 1 --default 0 --resampledmovingfilename /export2/mruiz/slicer-test/out-${ITER}-${HIST}-${SAM}.nrrd in[fixed.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/fixed.nrrd?view=co in[moving.nrrd]:http://www.na-mic.org/ViewVC/index.cgi/trunk/Libs/MRML/Testing/TestData/moving.nrrd?view=co");
		System.out.println(stmt);
	}
}

//@AnnonREXMember(pattern="\\s*\\$\\{([a-zA-Z]\\w*(\\([\\w,]*\\))?)\\}\\s*=\\s*(\\[([^\\[\\]]*?)\\]|f:[a-zA-Z]\\w*\\(([^\\(\\)]*?)\\))", min=0)
//@AnnonREXMember(pattern=" \s*\$\{[a-zA-Z]\w*(\([\w,]*\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))", min=0)
//(^(\s*\$\{([a-zA-Z]\w*(\([\w,]*\)){0,1})\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))){0,})(.*)

//                       (\s*\$\{[a-zA-Z]\w*(\([\w,]*\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))){0,}.*
//						 (\s*\$\{[a-zA-Z]\w*(\(([\w,]*){1,}\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))){0,}.*
//						 (\s*\$\{[a-zA-Z]\w*(\(([\w]*[,]?){1,}\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))){0,}.*
//                       (\s*\$\{[a-zA-Z]\w*(\(([\w]*[,]?){1,}\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\)]*?)\))\s*){0,}.*
//                       (\s*\$\{[a-zA-Z]\w*(\(([\w]*[,]?){1,}\))?\}\s*=\s*(\[([^\[\]]*?)\]|f:[a-zA-Z]\w*\(([^\(\),]*[,]?){1,}\))\s*){0,}.*

@REXConfig4Class(rexPieces={"vars", "template"})
class Statement {
	@REXConfig4ListElement(min=0)
	public List<VarDefinition> vars;
	@REXConfig4String(pattern=@REXConfig4Field(field=".*"))
	public String template;
	public List<VarDefinition> getVars() { return vars; }
	public void setVars(List<VarDefinition> vars) { this.vars = vars; }
	public String getTemplate() { return template; }
	public void setTemplate(String template) { this.template = template; }
	public String toString() { return vars.toString() + " onto " + template; }
}

//@REXConfig4Class(rexPieces={"\\s*", "declaration", "\\s*=\\s*", "value", "\\s*"})
@REXConfig4Class(rexPieces={"\\s*", "declaration", "\\s*=\\s*", "value|functionInvocation", "\\s*"})
class VarDefinition {
	public VarDeclaration declaration;
	public VarValue value;
	public FunctionInvocation functionInvocation;
	public VarDeclaration getDeclaration() { return declaration; }
	public void setDeclaration(VarDeclaration declaration) { this.declaration = declaration; }
	public VarValue getValue() { return value; }
	public void setValue(VarValue value) { this.value = value; }
	public String toString() { return declaration.toString() + " === " + value + " OR " + functionInvocation; }
	public FunctionInvocation getFunctionInvocation() { return functionInvocation; }
	public void setFunctionInvocation(FunctionInvocation functionInvocation) { this.functionInvocation = functionInvocation; }
}

@REXConfig4Class(rexPieces={"\\$\\{", "id", "dims", "\\}"})
class VarDeclaration {
	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	public String id;
	@REXConfig4String(optional=true, pattern=@REXConfig4Field(prefix="\\(", suffix="\\)"))
	@REXConfig4ListElement(pattern=@REXConfig4Field(field="[\\w]*", suffix="[,]?"), min=1)
	public List<String> dims;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public List<String> getDims() { return dims; }
	public void setDims(List<String> dims) { this.dims = dims; }
	public String toString() { return id + " -> " + dims; }
}

@REXConfig4Class(rexPieces={"value"})
class VarValue {
	@REXConfig4String(pattern=@REXConfig4Field(prefix="\\[", field="([^\\[\\]]*)", suffix="\\]"))
	public String value;
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
	public String toString() { return value; }
}

@REXConfig4Class(rexPieces={"f:", "functionName", "\\(", "params", "\\)"})
class FunctionInvocation {
	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	public String functionName;
	@REXConfig4String()
	@REXConfig4ListElement(pattern=@REXConfig4Field(field="[^\\(\\),]*", suffix="[,]?"), min=1)
	public List<String> params;
	public String getFunctionName() { return functionName; }
	public void setFunctionName(String functionName) { this.functionName = functionName; }
	public List<String> getParams() { return params; }
	public void setParams(List<String> params) { this.params = params; }
	public String toString() { return functionName + "<" + params + ">"; }
}

