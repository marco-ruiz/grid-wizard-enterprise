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

package org.gwe.p2elv2.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValueSpace;
import org.gwe.utils.rex.REXUtils;

/**
 * @author Marco Ruiz
 * @since Aug 11, 2008
 */
public class PFRegExp extends PFunction {

	public PFRegExp() { super("regExp"); }
	
    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
    	PVarValueSpace result = new PVarValueSpace();
    	
    	Params par = new Params(params);
    	if (params.size() < 4) return result;
    	
	    String[] regexps = new String[]{par.prefixExp, par.matchExp, par.suffixExp};
		List<List<String>> fieldSources = REXUtils.findRepeatsStrong(par.source, regexps, true);
		
		if (par.matchIndex < 0) {
		    for (List<String> fldSource : fieldSources) 
		    	result.add(fldSource.get(1));
		} else {
			String resVal = (fieldSources.size() > par.matchIndex) ? fieldSources.get(par.matchIndex).get(1) : "";
			result.add(resVal);
		}
    	
	    return result;
    }

	public boolean isSingleValue(List<String> params) {
		return new Params(params).matchIndex >= 0;
	}

	class Params {
    	private int matchIndex = -1;
    	private String source;
    	private String prefixExp;
    	private String matchExp;
    	private String suffixExp;
    	
    	public Params(List<String> params) {
    		try {
    			matchIndex = Integer.parseInt(safelyGet(params, 0));
    		} catch(Exception e) {}
    		source        = safelyGet(params, 1);
    		prefixExp     = safelyGet(params, 2);
    		matchExp      = safelyGet(params, 3);
    		suffixExp     = safelyGet(params, 4);
    	}
    	
    	private String safelyGet(List<String> params, int index) {
    		return (params.size() > index) ? params.get(index) : "";
    	}
    }

    public static void main(String[] args) {
/*  
		$regexp(/home/user/data/file, /, [^/]*, /)                                      = home, user, data, file
		$regexp(/home/user/data/file, /, [^/]*, $)                                      = file
		$regexp(cmd --aIndex 1 --bIndex 2 --other 4 --cIndex 3, \s*--, [^\s]*, Index)   = a, b, c
		$regexp(http://host/path, ://, [^/]*, /)                                        = host
*/    	

    	testCase("");
    	testCase("-1");
    	testCase("0");
    	testCase("1");
    	testCase("2");
    	testCase("3");
    	testCase("4");
    	testCase("5");
    }

	private static void testCase(String maxStr) {
	    testParameters(maxStr, "/home/user/data/file", "/", "[^/]*");
    	testParameters(maxStr, "/home/user/data/file", "/", "[^/]*", "$");
    	testParameters(maxStr, "cmd --aIndex 1 --bIndex 2 --other 4 --cIndex 3", "\\s*--", "[^\\s]*", "Index");
    	testParameters(maxStr, "http://host/path", "://", "[^/]*", "/");
		System.out.println();
    }
    
    private static void testParameters(String... params) {
    	PFRegExp testObj = new PFRegExp();
    	PVarValueSpace values = testObj.calculateValues(new ArrayList<String>(Arrays.asList(params)), null);
		System.out.println(values);
    }
}

