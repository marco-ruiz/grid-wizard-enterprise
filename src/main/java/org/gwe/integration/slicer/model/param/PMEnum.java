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

package org.gwe.integration.slicer.model.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwe.integration.slicer.model.ParameterModel;

/**
 * @author Marco Ruiz
 * @since Feb 4, 2008
 */
public class PMEnum<ELE_TYPE> extends ParameterModel {

	public static final String ENUM_SUFFIX = "-enumeration";
	
	private List<ELE_TYPE> elements = new ArrayList<ELE_TYPE>();

	public List<ELE_TYPE> getElements() {
		return elements;
	}

	public void addElement(List<ELE_TYPE> elements) {
		this.elements = elements;
	}

	public PMEnum(String primitiveValueType, String label, String longFlag, String description, String defaultValue, ELE_TYPE... elements) {
		super(primitiveValueType + ENUM_SUFFIX, label, longFlag, description, defaultValue);
		this.elements = Arrays.asList(elements);
	}
}


/*
	    <string-enumeration>
	      <name>stringChoice</name>
	      <flag>e</flag>
	      <longflag>enumeration</longflag>
	      <description>An enumeration of strings</description>
	      <label>String Enumeration Parameter</label>
	      <default>foo</default>
	      <element>foo</element>
	      <element>"foobar"</element>
	      <element>foofoo</element>
	    </string-enumeration>
	  
	  
	<parameters>
		<label>${group.label}</label>
		<description>${group.description}</description>
		#foreach(${param} in ${group.parameters})

		<${param.valueType}>
			<name>${param.name}</name>
			<flag>${param.flag}</flag>
			<longflag>${param.longFlag}</longflag>
			<label>${param.label}</label>
			<description>${param.description}</description>
			<default>${param.defaultValue}</default>
		</${param.valueType}>
	#end</parameters>
	  
*/
