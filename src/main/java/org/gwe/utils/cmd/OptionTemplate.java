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

package org.gwe.utils.cmd;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanWrapper;

/**
 * 
 * @author Marco Ruiz
 * @since Jul 12, 2007
 */
public class OptionTemplate {

	private static final char NO_SHORT_FLAG = '-';
	
	private char shortFlag = NO_SHORT_FLAG;
	private String longFlag;
	private String description;
	private String defaultValue;
	private String propertyName = null;
	
	private String toString;
	
	public OptionTemplate(String longFlag, String description) {
		this(longFlag, NO_SHORT_FLAG, longFlag, description, null);
	}
	
	public OptionTemplate(String longFlag, String description, String defVal) {
		this(longFlag, NO_SHORT_FLAG, longFlag, description, defVal);
	}
	
	public OptionTemplate(char shortFlag, String longFlag, String description) {
		this(longFlag, shortFlag, longFlag, description, null);
	}
	
	public OptionTemplate(char shortFlag, String longFlag, String description, String defVal) {
		this(longFlag, shortFlag, longFlag, description, defVal);
	}
	
	public OptionTemplate(String propName, String longFlag, String description, String defVal) {
		this(propName, NO_SHORT_FLAG, longFlag, description, defVal);
	}
	
	public OptionTemplate(String propName, char shortFlag, String longFlag, String description) {
		this(propName, shortFlag, longFlag, description, null);
	}
	
	public OptionTemplate(String propName, char shortFlag, String longFlag, String description, String defVal) {
		this.propertyName = propName;
		this.shortFlag = shortFlag;
		this.longFlag = longFlag;
		this.description = description;
		this.defaultValue = defVal;
		toString = createToString("\t\t");
	}
	
	private String createToString(String filler) {
		String shortDesc = (shortFlag != '-') ? "\t-" + shortFlag + ", " : "";
		return shortDesc + "--" + longFlag + filler + getFullDescription() + "\n";
	}

	private String getFullDescription() {
		String defValMsg = (defaultValue != null && !defaultValue.equals("")) ? "Defaults to '" + defaultValue + "'" :  "No default value";
		return description + ". " + defValMsg + ".";
	}

	public char getShortFlag() {
		return shortFlag;
	}

	public String getLongFlag() {
		return longFlag;
	}

	public String getDescription() {
		return description;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getPropertyName() {
		return propertyName;
	}
	
	public void setObjectProperty(BeanWrapper appObjectWrapper, Object value) {
		if (propertyName == null) return;
		appObjectWrapper.setPropertyValue(propertyName, value);
	}

	public String readOptionValue(String[] args) {
		return readOptionValue(args, defaultValue);
	}
	
	public String readOptionValue(String[] args, String defaultValue) {
		String res = readUnformattedOptionValue(args, defaultValue);
		if (res == null) return res;
		return (res.matches("^\".*\"$")) ? res.substring(1, res.length() - 1) : res;
	}

	private String readUnformattedOptionValue(String[] args, String defaultValue) {
		String longName = "--" + longFlag;
		String shortName = (shortFlag != '-') ? "-" + shortFlag : null; // If empty string then it will never match
		
		for (int idx = 0; idx < args.length; ++idx) {
		    String currArg = args[idx];
			if (currArg.equals(longName) || currArg.equals(shortName)) return hasValueAssigned(args, idx) ? args[idx + 1] : "";
		}
		    	
		for (int idx = 0; idx < args.length; ++idx) {
		    String currArg = args[idx];
		    if (startsWith(currArg, longName, shortName)) return currArg.substring(currArg.indexOf("=") + 1);
		}

		return defaultValue;
	}

	public String[] removeOptionFromArray(String[] args) {
		List<String> results = new ArrayList<String>(); 
		
		String longName = "--" + longFlag;
		String shortName = (shortFlag != '-') ? "-" + shortFlag : null; // If empty string then it will never match

		for (int idx = 0; idx < args.length; ++idx) {
		    String currArg = args[idx];
			if (currArg.equals(longName) || currArg.equals(shortName)) {
				if (hasValueAssigned(args, idx)) idx++;
			} else {
			    if (!startsWith(currArg, longName, shortName)) results.add(currArg);
			}
		}
		
		return results.toArray(new String[]{});
	}
	
	private boolean hasValueAssigned(String[] args, int index) {
		if (args.length <= index + 1) return false;
		String arg = args[index + 1];
		if (arg.length() == 0) return true;
		if (arg.charAt(0) == '-') {
			try {
				Double.parseDouble(arg);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}
	
	private boolean startsWith(String currArg, String longName, String shortName) {
		return currArg.startsWith(longName + "=") || (shortName != null && currArg.startsWith(shortName + "="));
	}
	
	public String toString() { return toString; }

	public String toString(int longFlagSize) {
		return createToString(createFiller(longFlagSize));
	}

	private String createFiller(int longFlagSize) {
		String filler = "";
		for (int count = 0; count < (longFlagSize - longFlag.length()); count++) filler  += " ";
		return filler;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof OptionTemplate)) return false;
		return ((OptionTemplate)obj).getLongFlag().equals(longFlag);
	}

	public int hashCode() {
		return toString.hashCode(); 
	}
}
