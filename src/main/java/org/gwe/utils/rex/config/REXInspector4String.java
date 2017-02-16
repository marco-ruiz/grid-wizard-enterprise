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
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Jul 26, 2008
 */
public class REXInspector4String {

	private Field field;
	private REXConfig4String cfg;
	private REXInspector4ListElement eleInspector;
	private String pattern;
	private boolean listField;
	
	public REXInspector4String(Field field) throws SecurityException, NoSuchFieldException {
		this.field     = field;
		this.listField = List.class.isAssignableFrom(field.getType());
		this.cfg       = field.getAnnotation(REXConfig4String.class);
		this.eleInspector = new REXInspector4ListElement(field);
		
		this.pattern = (listField) ? eleInspector.getFullPattern() :
			(cfg != null) ? cfg.pattern().field() : REXInspector4Class.getConfig(field.getType()).getPattern();
	}

	public Field getField() {
    	return field;
    }

	public String getPattern() {
    	return pattern;
    }

	public String getFullPattern() {
		String fullPattern = (cfg != null) ? cfg.pattern().prefix() + pattern + cfg.pattern().suffix() : pattern;
		return (cfg != null && cfg.optional()) ? "(" + fullPattern + ")?" : fullPattern;
    }

	public boolean isListField() {
    	return listField;
    }

	public REXInspector4ListElement getEleInspector() {
	    return eleInspector;
    }
	
	public String[] getPatterns() {
		return (cfg != null) ? new String[]{ cfg.pattern().prefix(), pattern, cfg.pattern().suffix() } : new String[]{"", pattern, ""};
    }
}
