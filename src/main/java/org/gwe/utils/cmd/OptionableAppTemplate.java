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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.utils.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * @author Marco Ruiz
 * @since Jul 13, 2007
 */
public class OptionableAppTemplate {
	
	public final OptionTemplate OPT_HELP = new OptionTemplate('h', "help", "Help. Displays command usage");
	public final OptionTemplate OPT_INTERACT = new OptionTemplate("interactive", "Runs application in interactive mode, requesting the parameters to the user one by one.");
    
	private List<OptionTemplate> templatesInOrder = new ArrayList<OptionTemplate>();
	private Map<OptionTemplate, String> options = new HashMap<OptionTemplate, String>();
    
    public OptionableAppTemplate() {
    	options.put(OPT_HELP, null);
    	options.put(OPT_INTERACT, null);
	}
    
    public OptionableAppTemplate(OptionTemplate... templates) {
    	this();
    	addOptions(templates);
	}
    
	public OptionableAppTemplate addOptions(OptionTemplate... options) {
		for (OptionTemplate template : options)
			addOption(template, template.getDefaultValue());

		return this;
	}
	
	public void resetArgs() {
		addOptions(templatesInOrder.toArray(new OptionTemplate[]{}));
	}
	
	public OptionTemplate addArg(OptionTemplate template) {
		return addOption(template, template.getDefaultValue());
	}
	
	public OptionTemplate addOption(OptionTemplate template, Object value) {
		templatesInOrder.remove(template);
		templatesInOrder.add(template);
		String strVal = (value != null) ? value.toString() : null; 
		options.put(template, strVal);
		return template;
	}
	
	public void removeOption(OptionTemplate template) {
		templatesInOrder.remove(template);
		options.remove(template);
	}
	
	public String[] getInvocationArgs() {
		List<String> result = new ArrayList<String>();
		for (OptionTemplate template : templatesInOrder) {
			result.add("-" + String.valueOf(template.getShortFlag()));
			result.add(options.get(template));
		}
		return result.toArray(new String[]{});
	}
    
//	public String getInvocationArgsAsStr() { return StringUtils.getArrayAsAsStr(getInvocationArgs()); }
    
    public <APP_TYPE> APP_TYPE loadArgs(String[] args, APP_TYPE appObject) {
    	loadArgs(args);
    	return populateAppObject(appObject);
    }
    
    public void loadArgs(String[] args) {
    	resetArgs();
    	for (OptionTemplate option : options.keySet()) 
    		options.put(option, option.readOptionValue(args));
    	
    	if (getArg(OPT_HELP) != null) {
    		System.out.println(getUsage());
    		System.exit(0);
    	}
    	
    	if (getArg(OPT_INTERACT) != null) {
    		System.out.println("PLEASE ENTER THE PARAMETERS AS THEY ARE REQUESTED.\n" +
    				"To use a field's default value just leave it blank and press return.\n" +
    				"======================================================================");
        	for (OptionTemplate option : templatesInOrder) readOptionFromConsole(option);
    	}
    }
    
    public String[] getWithoutOptionsArgs(String[] args) {
    	for (OptionTemplate option : options.keySet()) 
    		args = option.removeOptionFromArray(args);
    	return args;
    }
    
    public String getWithoutOptionsArgsAsStr(String[] args) { return StringUtils.getArrayAsStr(getWithoutOptionsArgs(args)); }
    
	private void readOptionFromConsole(OptionTemplate option) {
		try {
			String value = readNextField(option.getDescription() + "\n" + option.getLongFlag());
			if (value == null || "".equals(value)) value = option.getDefaultValue();
			options.put(option, value);
		} catch (IOException e) {
			System.out.print("There was an IO error trying to collect your information. " +
					"The application will use the default value of " + option.getDefaultValue() + " for " + option.getLongFlag());
		}
	}
    
	public String getArg(OptionTemplate key) { return options.get(key); }
	
	public Integer getArgAsInt(OptionTemplate key) {
		try {
			return Integer.parseInt(options.get(key));
		} catch (Exception e) {
			return null;
		}
	}
	
	public Long getArgAsLong(OptionTemplate key) {
		try {
			return Long.parseLong(options.get(key));
		} catch (Exception e) {
			return null;
		}
	}
	
	public <APP_TYPE> APP_TYPE populateAppObject(APP_TYPE appObject) {
		BeanWrapper appObjectWrapper = new BeanWrapperImpl(appObject);
		for (OptionTemplate template : templatesInOrder) 
			template.setObjectProperty(appObjectWrapper, options.get(template));
		return appObject;
	}
	
    public String getUsage() {
    	String usage;
    	usage = "Usage: java " + this.getClass().getName() + " [OPTION]\n\nOptions:\n";

    	int longFlagSize = 0;
    	for (OptionTemplate option : templatesInOrder) longFlagSize = Math.max(longFlagSize, option.getLongFlag().length());
    	for (OptionTemplate option : templatesInOrder) usage += option.toString(longFlagSize);
    	return usage; 
    }
    
    public String toString() {
    	return getUsage();
    }
    
    private String readNextField(String fieldName) throws IOException {
        System.out.print(fieldName + ": ");
	    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line = reader.readLine();
        if (line == null) return null;
		return line;
    }
    
    public OptionableAppTemplate clone() {
    	OptionableAppTemplate result = new OptionableAppTemplate(templatesInOrder.toArray(new OptionTemplate[]{}));
   		result.options.putAll(options);
   		return result;
    }
}

