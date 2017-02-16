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

import java.util.HashMap;
import java.util.Map;

import org.gwe.drivers.fileSystems.staging.FilesStager;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since Aug 13, 2008
 */
public class PStatementContext {

	private ContextVariables systemVars  = new ContextVariables(PVariable.SYS_VAR_PREFIX);
	private ContextVariables runtimeVars = new ContextVariables(PVariable.RUN_VAR_PREFIX);
	
	private String workspace;
	private KeyStore keys;
	private FilesStager stager;
	
	public PStatementContext(String workspace, KeyStore keys, FilesStager stager) {
	    this.workspace = workspace;
	    this.keys = keys;
	    this.stager = stager;
    }

	public void addSystemVar(String name, String value) {
		systemVars.addVar(name, value);
    }

	public void addRuntimeVar(String name, String value) {
		runtimeVars.addVar(name, value);
    }
	
	public Map<String, String> getSystemVars() {
    	return systemVars;
    }

	public Map<String, String> getRuntimeVars() {
    	return runtimeVars;
    }

	public String getWorkspace() {
    	return workspace;
    }

	public KeyStore getKeys() {
	    return keys;
    }

	public FilesStager getStager() {
    	return stager;
    }
}

class ContextVariables extends HashMap<String, String> {
	
	private String prefix;
	
	public ContextVariables(String prefix) {
	    this.prefix = prefix + "_";
    }
	
	public void addVar(String name, String value) {
		put(prefix + name, value);
	}
}

