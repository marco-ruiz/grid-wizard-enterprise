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

package org.gwe.integration.slicer.model;

import org.gwe.utils.cmd.OptionTemplate;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class ParameterModel extends OptionTemplate {
	
	private String valueType; // returns the parameter's tag, e.g. <integer>, <image>, etc.
	private String name = "";
	private String longFlag = null;
	private String label = "";
	private int index = -1;
	private String channel = null;
	private String filler = "";
	
	public ParameterModel(String valueType, String label, String longFlag, String description, String defaultValue) {
		super(longFlag, description, defaultValue);
		this.valueType = valueType;
		this.label = label;
		setLongFlag(longFlag);
	}

	public String getValueType() {
		return valueType;
	}
	
	public void setValueType(String tag) {
		this.valueType = tag;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		if (longFlag == null || longFlag.equals("")) 
			setLongFlag(name);
	}
	
	public String getLongFlag() {
    	return longFlag;
    }

	public void setLongFlag(String longFlag) {
		while (longFlag != null && longFlag.startsWith("-")) longFlag = longFlag.substring(1);
    	this.longFlag = longFlag;
    }

	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public int getIndex() {
    	return index;
    }

	public void setIndex(int index) {
    	this.index = index;
    }

	public String getFlag() {
		return "";
	}
	
	public void setFlag(String flag) {
//		setShortFlag();
	}
	
	public String getChannel() {
    	return channel;
    }

	public void setChannel(String channel) {
    	this.channel = channel;
    }

	public String getFiller() {
		return getFiller(true, true);
    }

	public String getFiller(boolean includeIndexIfPresent, boolean includeChannelIfPresent) {
		String result = (includeIndexIfPresent && index != -1) ? "<index>" + index + "</index>\n" : "";
		if (includeChannelIfPresent && channel != null) 
			result += "<channel>" + channel + "</channel>\n";
		
    	return result + filler;
    }

	public void setFiller(String filler) {
		if (filler != null) this.filler = filler;
    }
}


