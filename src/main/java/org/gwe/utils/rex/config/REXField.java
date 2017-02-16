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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Jul 27, 2008
 */
public class REXField {

	private Class targetClass;
	private List<Field> fields = new ArrayList<Field>();
	private String pattern = "";
	
	public REXField(Class targetClass, String currPattern) {
		this.targetClass = targetClass;
	    try {
	    	pattern = buildPattern(targetClass, currPattern); 
	    } catch (NoSuchFieldException e) {
		    pattern = currPattern;
	    }
    }

	private String buildPattern(Class targetClass, String currPattern) throws NoSuchFieldException {
		String result = "";
	    String[] fieldNames = currPattern.split("\\|");
	    for (String fldName : fieldNames) {
	    	if (!result.equals("")) result += "|"; 
	    	Field fld = targetClass.getDeclaredField(fldName);
	    	fields.add(fld);
			result += new REXInspector4String(fld).getFullPattern();
	    }
	    return (fieldNames.length > 1) ? "(" + result + ")" : result;
    }
	
	public List<Field> getFields() {
    	return fields;
    }

	public String getPattern() {
    	return pattern;
    }
}
