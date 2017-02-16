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

package org.gwe.persistence.model;

import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Aug 22, 2007
 */
public enum OSAppFolder {
	ROOT          (""), 
	HOME          (""), 
	BINARIES      ("bin"), 
	LIBRARIES     ("lib"), 
	CONFIGURATIONS("conf"), 
	BUNDLES       ("dist"), 
	DOCUMENTS     ("doc"), 
	DATABASE      ("db"), 
	LOGS          ("logs"), 
	SCRIPTS       ("script"), 
	SPOOL         ("spool"),
	WORKSPACE     ("workspace"),
	VFS           ("vfs");
	
	private String defaultValue;
	
	OSAppFolder(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getRelativeToUserDir() {
		return getRelativeTo(System.getProperty("user.dir"));
	}
	
	public String getRelativeTo(String dir) {
		return (dir != null) ? IOUtils.concatenatePaths(dir, toString()) : null;
	}
	
	public String toString() {
		return defaultValue;
	}
}
