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

package org.gwe.app.client.web.view;

import java.util.List;

import org.gwe.GWEAppContext;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.web.HtmlLink;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2008
 */
public class HtmlPVarTable extends HtmlTable {
	
	public HtmlPVarTable(List<PVariable> vars) {
	    super("Variable", "Value Generator", "Parameters");
	    if (vars == null) return;
		for (PVariable var : vars) {
	        String params = var.getFunctionInvocation().getParams().toString();
	        params = params.substring(1, params.length() - 1);
	        String functionName = var.getFunctionInvocation().getFunctionName();
	        
	        boolean isFunction = GWEAppContext.getP2ELFunctionNames().contains(functionName);
	        String caption = new StringBuffer(functionName).append(" (").append(isFunction ? "function" : "macro").append(")").toString();
	        
			HtmlTableCell function = new HtmlTableCell(caption, "", getURLLink(functionName, isFunction));
			addRow(var.getFullName(), function, params);
        }
    }

	private HtmlLink getURLLink(String functionName, boolean isFunction) {
	    String baseURL = isFunction ? "http://www.gridwizardenterprise.org/guides/p2el-semantics.html" : "grid?expanded=true";
		String url = new StringBuffer(baseURL).append("#").append(functionName).toString();
		return new HtmlLink(isFunction, url);
    }
}
