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

package org.gwe.integration.slicer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.gwe.integration.slicer.model.ExecutableModel;
import org.gwe.utils.StringUtils;
import org.gwe.utils.cmd.ArgsList;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2007
 */
public abstract class AbstractCLMProxyApp {

	public static String ENV_SLICER_HOME = "SLICER_HOME";
	public static String PLUGINS_RELATIVE_DIR = "/lib/Slicer3/Plugins";
	
	private static Class[] classes = new Class[]{String.class};
	
	public static void main(String[] args) throws Exception {
		ArgsList argsList = new ArgsList(args);
		// First argument is the proxy application class name
		Class<? extends AbstractCLMProxyApp> proxyAppClass = (Class<? extends AbstractCLMProxyApp>) Class.forName(argsList.remove(0)); 
		proxyAppClassMain(proxyAppClass, argsList); 
	}
	
	protected static <PROXY_APP_TYPE extends AbstractCLMProxyApp> void proxyAppClassMain(Class<PROXY_APP_TYPE> proxyAppClass, List<String> argsList) throws Exception {
		// First argument is the proxy identifier
		PROXY_APP_TYPE proxyApp = proxyAppClass.getConstructor(classes).newInstance(argsList.remove(0));
		proxyAppMain(proxyApp, argsList); 
	}

	protected static <PROXY_APP extends AbstractCLMProxyApp> void proxyAppMain(PROXY_APP proxyApp, List<String> argsList) {
		String[] args = argsList.toArray(new String[]{});
		try {
			ExecutableModel model = new ExecutableModel(null, null, null);
			model.loadArgs(args, false);
			if (model.isXMLSelected()) {
			    System.out.println(proxyApp.generateProxyXML());
	    	} else if (model.isLogoSelected()) {
	    	} else {
	    		try {
	    			System.out.println("CLMP invoked with args: " + StringUtils.getArrayAsStr(args));
					proxyApp.runProxyApp(args);
				} catch (RemoteException e) {
					// TODO: Somehow output error message to slicer user
				}
	    	}
		} catch (Exception e) {
			// TODO: Report problem executing a phase of this proxy app
			System.out.println("CLMP invoked with args: " + StringUtils.getArrayAsStr(args));
			e.printStackTrace();
		}
	}

	public static String[] removeArgs(String[] args, int count) {
		String[] trimmedArgs = new String[args.length - count];
		for (int idx = count; idx < args.length; idx++) trimmedArgs[idx - count] = args[idx];
		return trimmedArgs;
	}
	
	protected String slicerHome;
	protected ExecutableModel xmlModel = null; // Optional

	public AbstractCLMProxyApp() {
		this.slicerHome = System.getenv(ENV_SLICER_HOME);
	}

	public String getPluginsDir() {
		return getPluginsDir(slicerHome);
	}

	public String getPluginsDir(String slicerHomeDir) {
		return slicerHomeDir + PLUGINS_RELATIVE_DIR;
	}

	public ExecutableModel getXmlModel() {
    	return xmlModel;
    }

	public void setXmlModel(ExecutableModel xmlModel) {
    	this.xmlModel = xmlModel;
    }

	public abstract String generateProxyXML() throws Exception;

	public abstract String runProxyApp(String[] args) throws Exception;

	protected String generateCLMErrorXML(IOException e) { return ""; }
}

