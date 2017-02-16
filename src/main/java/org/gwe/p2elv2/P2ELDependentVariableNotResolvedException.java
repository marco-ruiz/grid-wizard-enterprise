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

import java.util.Set;

import org.gwe.p2elv2.model.PVariable;

/**
 * @author Marco Ruiz
 * @since Jul 31, 2008
 */
public class P2ELDependentVariableNotResolvedException extends Exception {

	private PVariable var;
	private Set<String> dependencies;
	
	public P2ELDependentVariableNotResolvedException(Set<String> dependencies) {
		this(dependencies, null);
    }

	public P2ELDependentVariableNotResolvedException(Set<String> dependencies, PVariable var) {
		this.dependencies = dependencies;
		setVariable(var);
    }

	public void setVariable(PVariable var) {
		this.var = var;
    }

    public String getMessage() {
	    return "Variable " + var.getName() + " has unresolved or circular dependencies with variables " + dependencies;
    }
}

