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

package org.gwe.app;

import java.io.Serializable;

/**
 * @author Marco Ruiz
 * @since Oct 5, 2008
 */
public class Distribution implements Serializable {
	
	private String name;
	private String version;

	public String getName() {
    	return name;
    }

	public void setName(String name) {
    	this.name = name;
    }

	public String getVersion() {
    	return version;
    }

	public void setVersion(String version) {
    	this.version = version;
    }
	
	public String getVersionedName() {
		return name + "-" + version;
	}
}
