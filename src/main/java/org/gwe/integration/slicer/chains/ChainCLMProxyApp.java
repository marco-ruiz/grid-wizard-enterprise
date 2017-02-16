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

package org.gwe.integration.slicer.chains;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.integration.slicer.AbstractCLMProxyApp;
import org.gwe.integration.slicer.WrapperCLMProxyApp;
import org.gwe.integration.slicer.model.ExecutableModel;
import org.gwe.integration.slicer.model.ExecutableModelParser;
import org.gwe.integration.slicer.model.GroupModel;
import org.gwe.integration.slicer.model.ParameterModel;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.cmd.OptionTemplate;
import org.gwe.utils.cmd.OptionableAppTemplate;

/**
 * @author Marco Ruiz
 * @since Mar 25, 2008
 */
public class ChainCLMProxyApp extends AbstractCLMProxyApp {

	private static Log log = LogFactory.getLog(ChainCLMProxyApp.class);

	private static String ARG_PREFIX = "clm_";
	private static String TITLE_SEPARATOR = " | ";

	public static void main(String[] args) throws Exception { 
		proxyAppClassMain(ChainCLMProxyApp.class, new ArgsList(args)); 
	}
	
	private List<AbstractCLMProxyApp> clmApps = new ArrayList<AbstractCLMProxyApp>();
	private ChainDesc chain;

	public ChainCLMProxyApp(String chainDescriptorFile) throws Exception {
		chain = new ChainDesc(chainDescriptorFile);
		ExecutableModelParser descParser = new ExecutableModelParser();
		for (ChainCLMDesc clmDesc : chain.getCLMs()) {
			WrapperCLMProxyApp proxyApp = new WrapperCLMProxyApp(clmDesc.getId());
			proxyApp.setXmlModel(descParser.readExecutableModel(proxyApp.generateProxyXML()));
			clmApps.add(proxyApp);
		}
	}
	
	public String generateProxyXML() throws Exception {
		ExecutableModel result = new ExecutableModel(createTitle(), "Composite Chains", "", "org/gwe/integration/slicer/chains/chainsSEMTemplate.vm");

		for (int moduleIdx = 0; moduleIdx < clmApps.size(); moduleIdx++) {
			ExecutableModel currAppModel = clmApps.get(moduleIdx).getXmlModel();
			ChainCLMDesc currChainDesc = chain.getCLMs().get(moduleIdx);
			String groupTitlePrefix = currAppModel.getTitle() + " - ";
	        for (GroupModel groupModel : currAppModel.getGroups()) {
				String newLabel = groupTitlePrefix + groupModel.getLabel();
				GroupModel currGroup = result.addGroup(newLabel, groupModel.getDescription());
	            for (ParameterModel paramModel : groupModel.getParameters()) {
	            	// If the argument is not piped from the output of a previous CLM then add it to the XML
	            	if (!currChainDesc.getPipedArguments().containsKey(paramModel.getName()))
	            		currGroup.addParam(createProxiedParam(currChainDesc.getId(), paramModel));
	            }
            }
        }
		
		return result.getXML();
	}
	
	private ParameterModel createProxiedParam(String chainLinkId, ParameterModel param) {
		String flagsPrefix = createFlagPrefix(chainLinkId);
		String valueType = param.getValueType();
/*
		if ("file".equals(valueType) || "directory".equals(valueType) || "image".equals(valueType))
			valueType = "string";
*/
		String label = param.getLabel();
		String longFlag = flagsPrefix + param.getLongFlag();
		String description = param.getDescription();
		String defaultValue = param.getDefaultValue();
		
		ParameterModel result = new ParameterModel(valueType, label, longFlag, description, defaultValue);
		result.setName(longFlag);
		result.setFlag(flagsPrefix + param.getFlag());
		result.setFiller(param.getFiller(false, true));

		return result;
	}
	
	private String createFlagPrefix(String chainLinkId) {
		return appendArgPrefix(chainLinkId + "_");
	}
	
	private String appendArgPrefix(String flagId) {
		return ARG_PREFIX + flagId;
	}
	
	private String createTitle() {
	    String title = "";
		for (AbstractCLMProxyApp proxyApp : clmApps) 
			title += proxyApp.getXmlModel().getTitle() + TITLE_SEPARATOR;
		
		return title.substring(0, title.length() - TITLE_SEPARATOR.length());
    }

	public String runProxyApp(String[] args) throws Exception {
		String result = "";
		for (int moduleIdx = 0; moduleIdx < clmApps.size(); moduleIdx++) {
			ChainCLMDesc currChainDesc = chain.getCLMs().get(moduleIdx);
			List<String> argsList = constructCLMArgs(args, currChainDesc);
			Map<String, String> pipedArgsValues = readPipedArgsValues(args, currChainDesc.getPipedArguments());
			for (Map.Entry<String, String> clmArg : pipedArgsValues.entrySet()) {
				argsList.add("--" + clmArg.getKey());
				argsList.add(clmArg.getValue());
            }
			
			WrapperCLMProxyApp wrapperApp = (WrapperCLMProxyApp)clmApps.get(moduleIdx);
			String[] chainedModuleArgs = fixIndexedArgs(argsList, moduleIdx).toArray(new String[]{});
			result += wrapperApp.runProxyApp(chainedModuleArgs) + "\n===\n";
//			result += wrapperApp.createOSCommand(chainedModuleArgs) + "\n";
		}
//		writeScript("#!/bin/sh\n# Autogenerated chain script\n" + result);
		return result;
	}
	
	private void writeScript(String content) {
		try {
			FileOutputStream fos = new FileOutputStream(slicerHome + "/chains/chain.sh", false);
	        fos.write(content.getBytes());
        } catch (IOException e) {
        }
	}

	private List<String> constructCLMArgs(String[] args, ChainCLMDesc currChainDesc) {
	    List<String> result = new ArrayList<String>();
	    String flagsPrefix = createFlagPrefix(currChainDesc.getId());
	    for (int argIndex = 0; argIndex < args.length; argIndex++) {
	    	String newValue = args[argIndex].replace(flagsPrefix, "");
	    	if (!newValue.startsWith("--" + ARG_PREFIX)) { 
	    		result.add(newValue);
	    	} else {
	    		if (!args[argIndex + 1].startsWith("--")) argIndex++;
	    	}
	    }
	    
	    return result;
    }

	private Map<String, String> readPipedArgsValues(String[] args, Map<String, String> pipedArgsMappings) {
	    OptionableAppTemplate invocationTemplate = new OptionableAppTemplate();
		Map<String, OptionTemplate> chainedOptions = new HashMap<String, OptionTemplate>();
        for (String clmArg : pipedArgsMappings.keySet()) {
        	OptionTemplate option = new OptionTemplate(appendArgPrefix(pipedArgsMappings.get(clmArg)),"");
        	chainedOptions.put(clmArg, option);
        	invocationTemplate.addOption(option, null);
        }
	    invocationTemplate.loadArgs(args);
	    
	    Map<String, String> pipedArgsValues = new HashMap<String, String>();
	    for (Map.Entry<String, OptionTemplate> clmArg : chainedOptions.entrySet())
	    	pipedArgsValues.put(clmArg.getKey(), invocationTemplate.getArg(clmArg.getValue()));
	    
	    return pipedArgsValues;
    }
	
	private List<String> fixIndexedArgs(List<String> argsList, int appIndex) {
		Map<String, Integer> indexedArgs = new HashMap<String, Integer>();
		for (ParameterModel param : clmApps.get(appIndex).getXmlModel().getParameters())
	        if (param.getIndex() != -1) indexedArgs.put(param.getLongFlag(), param.getIndex());
		
		Map<Integer, String> indexedInvocations = new TreeMap<Integer, String>();
		List<String> result = new ArrayList<String>();
		for (int argIndex = 0; argIndex < argsList.size(); argIndex++) {
			String argInvoc = argsList.get(argIndex);
			String arg = argInvoc.replace("-", "");
			if (!indexedArgs.containsKey(arg)) {
				result.add(argInvoc);
			} else {
	    		String nextArg = (argsList.size() > argIndex + 1) ? argsList.get(argIndex + 1) : "";
	    		if (nextArg == null) nextArg = "";
	    		boolean isValueNextArg = !nextArg.startsWith("--");
				indexedInvocations.put(indexedArgs.get(arg), isValueNextArg ? nextArg : "");
				if (isValueNextArg) argIndex++;
			}
        }
		
		int count = 0;
		for (Map.Entry<Integer, String> entry : indexedInvocations.entrySet()) { 
			result.add(count, entry.getValue());
			count++;
		}
		
		return result;
    }
}


