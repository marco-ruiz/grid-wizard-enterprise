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

package org.gwe.p2elv2.model;

import java.util.List;

import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4ListElement;

/**
 * @author Marco Ruiz
 * @since Aug 4, 2008
 */
@REXConfig4Class(rexPieces={"varRefs"})
public class PVarReferences {

	@REXConfig4ListElement(min=0)
	private List<PVarRef> varRefs;

	public List<PVarRef> getVarRefs() {
    	return varRefs;
    }

	public void setVarRefs(List<PVarRef> varIds) {
    	this.varRefs = varIds;
    }
	
	public String toString() {
		return varRefs.toString();
	}
}
