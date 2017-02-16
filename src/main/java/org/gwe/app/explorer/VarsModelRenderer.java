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

package org.gwe.app.explorer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gwe.p2elv2.functions.PFConst;
import org.gwe.p2elv2.functions.PFCount;
import org.gwe.p2elv2.functions.PFDir;
import org.gwe.p2elv2.functions.PFLines;
import org.gwe.p2elv2.functions.PFMD5Hex;
import org.gwe.p2elv2.functions.PFMath;
import org.gwe.p2elv2.functions.PFRange;
import org.gwe.p2elv2.functions.PFRegExp;
import org.gwe.p2elv2.functions.PFUUID;
import org.gwe.p2elv2.functions.PFXPath;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Marco Ruiz
 * @since Feb 25, 2009
 */
public class VarsModelRenderer {
	
	public static int getVarIndex(String target, List<String> varNames) {
        for (int idx = 0; idx < varNames.size(); idx++) 
        	if (target.equals(varNames.get(idx))) 
        		return idx;
        
        return -1;
	}
	
	public String renderJS(OrderInfo order) {
		StringBuffer result = new StringBuffer(); 
		List<String> varNames = order.getDescriptor().getVarNames();
		result.append(createJSArray("var varNames", varNames));
		List<JobInfo> jobs = order.getJobs();
		result.append("var varValues = [];\n");
		for (JobInfo job : jobs) {
			StringBuffer jobJSArray = createJSArray("varValues[" + (job.getJobNum() - 1) + "]", getValues(job, varNames));
			result.append(jobJSArray);
        }
		
		result.append("var varDependencies = [];\n");
		VarsDependencies varsDep = new VarsDependencies(order);
		for (int idx = 0; idx < varNames.size(); idx++) {
			String arrayName = "varDependencies[" + idx + "]";
			StringBuffer jobJSArray = createJSArray(arrayName, varsDep.get(varNames.get(idx)), true);
			result.append(jobJSArray);
        } 

		return result.toString();
	}

    private List getValues(JobInfo job, List<String> varNames) {
    	List result = new ArrayList();
    	Map<String, Object> vars = job.getDescriptor().getPermutationValues();
    	
    	for (String varName : varNames) {
	        Object varValue = vars.get(varName);
	        String varValueStr = (varValue == null) ? "" : varValue.toString();
	        varValueStr = varValueStr.replace("\n", "\\\n").replace("'", "\\'");
	        result.add(varValueStr);
        }
	    return result;
    }

	private StringBuffer createJSArray(String arrayName, Collection elements) {
		return createJSArray(arrayName, elements, false);
    }

	private StringBuffer createJSArray(String arrayName, Collection elements, boolean integers) {
		StringBuffer result = new StringBuffer();
		appendValues(result, elements, integers);
		result.insert(0, arrayName + " = [");
		result.append("];\n");
		return result;
    }

	private void appendValues(StringBuffer result, Collection elements, boolean integers) {
		boolean notFirstElement = false;
	    for (Object ele : elements) {
	    	if (notFirstElement) result.append(",");  
	    	if (!integers) result.append("'");
			result.append(ele);
	    	if (!integers) result.append("'");
			notFirstElement = true;
		}
    }
	
	public static void main(String[] args) throws FileNotFoundException {
		new PFConst();
		new PFCount();
		new PFDir();
		new PFLines();
		new PFMath();
		new PFMD5Hex();
		new PFRange();
		new PFRegExp();
		new PFUUID();
		new PFXPath();
		
		
        XStream result = new XStream(new DomDriver());
		InputStream fis = new FileInputStream("/Users/admin/work/eclipse-ws/gwe-core/src/main/slider/order.xml");
//		InputStream fis = new FileInputStream("/Users/admin/work/eclipse-ws/gwe-core/src/temp/order.xml");
        OrderInfo order = (OrderInfo) result.fromXML(fis);
        
        System.out.println(new VarsModelRenderer().renderJS(order));
	}
}

class VarsDependencies extends TreeMap<String, Set<Integer>> {
	
	public static Set<Integer> NON_PERMUTABLE = new TreeSet<Integer>();
	
	static {
		NON_PERMUTABLE.add(-1);
	}
	
	private List<String> varNames;
	private List<PVariable> vars;
	
	public VarsDependencies(OrderInfo order) {
		varNames = order.getDescriptor().getVarNames();
		vars = ((POrderDescriptor)order.getDescriptor()).getP2ELStatementObj().getVars();
		
		// Load dependencies content

		for (PVariable var : vars) 
			put(var.getFullName(), getDependencies(var));
		
		for (int idx = 0; idx < varNames.size(); idx++) {
			String varName = varNames.get(idx);
			put(varName, getDeepDependencies(varName));
		}
		
		for (PVariable var : vars)
			if (!var.isPermutable()) put(var.getFullName(), NON_PERMUTABLE);
	}

	private Set<Integer> getDependencies(PVariable var) {
	    Set<Integer> result = new TreeSet<Integer>();
	    for (String dependency : var.getVarDependencyNames())
	    	result.add(VarsModelRenderer.getVarIndex(dependency, varNames));

	    result.remove(-1);
	    return result;
    }
	
	private Set<Integer> getDeepDependencies(String varName) {
		Set<Integer> result = get(varName);
		if (result.isEmpty()) return result;
		Set<Integer> deepDependencies = new HashSet<Integer>();
		for (Integer depIndex : result) 
			deepDependencies.addAll(getDeepDependencies(varNames.get(depIndex)));
		
		result.addAll(deepDependencies);
		return result ;
	}
}
