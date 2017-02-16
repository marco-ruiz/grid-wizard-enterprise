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

package org.gwe.utils.rex;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.gwe.utils.rex.config.REXField;
import org.gwe.utils.rex.config.REXInspector4Class;
import org.gwe.utils.rex.config.REXInspector4String;

/**
 * @author Marco Ruiz
 * @since Jul 23, 2008
 */
public class REXParser {

	public static <MODEL_TYPE> MODEL_TYPE createModel(Class<MODEL_TYPE> modelClass, String src) throws REXException {
		return createModel(modelClass, src, false);
	}
	
	public static <MODEL_TYPE> MODEL_TYPE createModel(Class<MODEL_TYPE> modelClass, String src, boolean preCleaunUpSpaces) throws REXException {
		try {
	        return populateModel(modelClass.newInstance(), src, preCleaunUpSpaces);
        } catch (Exception e) {
			throw new REXException("Could not create an instance of class '" + modelClass + "'", e);
        }
	}
	
	public static <MODEL_TYPE> MODEL_TYPE populateModel(MODEL_TYPE model, String src, boolean preCleaunUpSpaces) throws REXException {
		if (preCleaunUpSpaces) src = src.replaceAll("\\s{1,}", " ").trim();
		try {
			List<REXField> patternFields = REXInspector4Class.getConfig(model.getClass()).getPatternFields();
			List<String> matches = REXUtils.findSequentialMatches(src, patternFields);
			
			for (int idx = 0; idx < patternFields.size(); idx++) {
				REXField patField = patternFields.get(idx);
				for (Field field : patField.getFields()) {
					Object fieldValue = createFieldValue(model, matches.get(idx), field);
					if (fieldValue != null) { 
						setProperty(model, field, fieldValue);
						break;
					}
				}
	        }
	
			return model;
		} catch (Exception e) {
			throw new REXException("REX parser encountered a problem while creating a '" + model.getClass() + "' using source '" + src + "'", e);
		}
	}
	
	private static <MODEL_TYPE> Object createFieldValue(MODEL_TYPE model, String src, Field field) throws Exception {
		REXInspector4String inspector = new REXInspector4String(field);
		String matchSource = REXUtils.findSequentialMatches(src, inspector.getPatterns()).get(1);
		return (matchSource == null) ? null :
			inspector.isListField() ? buildListProperty(matchSource, field, inspector) : buildSimpleProperty(matchSource, field.getType());
	}

	private static <ELE_TYPE> List<ELE_TYPE> buildListProperty(String matchSource, Field field, REXInspector4String config) throws Exception, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Class<ELE_TYPE> eleClass = (Class<ELE_TYPE>) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
	    List<ELE_TYPE> result = new ArrayList<ELE_TYPE>();
	    if (!matchSource.equals("")) {
		    List<List<String>> fieldSources = REXUtils.findRepeats(matchSource, config.getEleInspector().getPatterns(), true);
		    for (List<String> fldSource : fieldSources) 
		    	result.add(buildSimpleProperty(fldSource.get(1), eleClass));
	    }
	    return result;
    }

	private static <ELE_TYPE> ELE_TYPE buildSimpleProperty(String matchSource, Class<ELE_TYPE> fieldClass) throws Exception {
	    return (fieldClass.equals(String.class)) ? (ELE_TYPE) matchSource : createModel(fieldClass, matchSource);
    }

	private static <MODEL_TYPE> void setProperty(MODEL_TYPE model, Field field, Object modelValue) throws Exception {
	    // Introspect setter
		String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method setter = field.getDeclaringClass().getDeclaredMethod(setterName, new Class[]{field.getType()});
        
        // Set property
        setter.invoke(model, new Object[]{modelValue});
    }
}

