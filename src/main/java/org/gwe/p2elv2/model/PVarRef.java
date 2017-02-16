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

import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Aug 5, 2008
 */
@REXConfig4Class(rexPieces={".*?\\$\\{\\s*", "name", "dimension", "\\s*\\}.*?"})
public class PVarRef {
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[a-zA-Z]\\w*"))
	private String name;
	
	@REXConfig4String(optional=true, pattern=@REXConfig4Field(prefix="[.]", field="\\w*?"))
	private String dimension = null;
	
	public String getName() { 
		return name; 
	}
	
	public void setName(String id) { 
		this.name = id; 
	}
	
	public String getDimension() { 
		return dimension; 
	}
	
	public void setDimension(String dim) { 
		dimension = dim; 
	}
	
	public String toString() {
		return "${" + name + "." + dimension + "}";
	}
}
