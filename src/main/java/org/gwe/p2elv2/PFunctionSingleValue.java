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

import java.util.List;

/**
 * @author Marco Ruiz
 * @since Aug 19, 2008
 */
public abstract class PFunctionSingleValue extends PFunction {

    public PFunctionSingleValue(String name) { super(name); }

	public boolean isSingleValue(List<String> params) {
		return true;
	}

	public final PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
    	PVarValueSpace result = new PVarValueSpace();
    	result.add(calculateValue(params, ctx));
	    return result;
    }
    
    public abstract PVarValue calculateValue(List<String> params, PStatementContext ctx);
}
