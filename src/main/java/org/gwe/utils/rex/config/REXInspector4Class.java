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

package org.gwe.utils.rex.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Ruiz
 * @since Jul 26, 2008
 */
public class REXInspector4Class {

	private static Map<Class, REXInspector4Class> cache = new HashMap<Class, REXInspector4Class>();
	
	public static REXInspector4Class getConfig(Class targetClass) throws SecurityException, NoSuchFieldException {
		REXInspector4Class result = cache.get(targetClass);
		 if (result == null) {
			 result = new REXInspector4Class(targetClass);
			 cache.put(targetClass, result);
		 }
		 return result;
	}
	
	private String pattern = "";
	private List<REXField> patternFields = new ArrayList<REXField>();
	
	private REXInspector4Class(Class targetClass) throws SecurityException {
		REXConfig4Class cfg = (REXConfig4Class) targetClass.getAnnotation(REXConfig4Class.class);
		if (cfg == null || cfg.rexPieces() == null) return;
		for (String currPattern : cfg.rexPieces()) {
			REXField patField = new REXField(targetClass, currPattern);
		    pattern += patField.getPattern();
		    patternFields.add(patField);
        }
	}
	
	public String getPattern() {
		return pattern;
	}
	
	public List<REXField> getPatternFields() {
	    return patternFields;
    }

	public String toString() {
		return pattern;
	}
}
