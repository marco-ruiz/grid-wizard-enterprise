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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;
import org.gwe.p2elv2.PVarValueSpace;
import org.gwe.utils.IOUtils;


/**
 * @author Marco Ruiz
 * @since Aug 11, 2008
 */
public class PFLines extends PFunction {

	private static Log log = LogFactory.getLog(PFLines.class);

	public PFLines() { super("lines"); }

    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
        PVarValueSpace result = new PVarValueSpace();
        String source = params.get(0);

		try {
			String content = readSource(source, ctx);
			for (String line : content.split("\n")) result.add(new PVarValue(line));
			if (result.size() == 0) result.add(new PVarValue(""));
        } catch (Exception e) {
        	e.printStackTrace();
        	log.warn(e);
        }
		
    	return result;
    }
    
    private String readSource(String source, PStatementContext ctx) {
    	try {
	        InputStream is = ctx.getKeys().createFileLink(source).createHandle().getInputStream();
			return IOUtils.readStream(is);
        } catch (Exception e) {
        	return source;
        }
    }
    
    public static void main(String[] args) {
    	List<String> params = new ArrayList<String>();
//    	params.add("/Users/admin/work/eclipse-ws/gwe-core/src/temp/test.txt");
    	params.add("Marco,12,0.5\nAntonio,24,0.1\nRuiz,36,0.7\nHuapaya,48,1.9");
    	
    	PFLines function = new PFLines();
		PVarValueSpace result = function.calculateValues(params, null);
		System.out.println(result);
		
		for (PVarValue varValue : result) System.out.println(varValue);
    }
}

