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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.gwe.utils.IOUtils;
import org.gwe.utils.StringUtils;


/**
 * @author Marco Ruiz
 * @since Dec 18, 2007
 */
public class WrapperCLMProxyApp extends AbstractCLMProxyApp {

	protected String proxiedModuleName;

	public WrapperCLMProxyApp(String proxiedModuleName) {
		this.proxiedModuleName = proxiedModuleName;
	}

	public String getProxiedModuleName() {
    	return proxiedModuleName;
    }

	public String getProxiedModuleInvocation() {
		return getModuleInvocation(slicerHome);
	}
	
	public String getModuleInvocation(String slicerHomeDir) {
		return slicerHomeDir + "/Slicer3 --launch " + IOUtils.concatenatePaths(getPluginsDir(slicerHomeDir), proxiedModuleName);
	}
	
	protected String generateCLMErrorXML(IOException e) {
		// TODO Generate XML descriptor error and output it to present the user with a CLM with the error message in the 'Help' tab
		return "";
	}
	
	public String generateProxyXML() {
		return executeOSCommand(getProxiedModuleInvocation() + " --xml");
	}
	
	public String runProxyApp(String[] args) throws Exception {
		return executeOSCommand(createOSCommand(args));
	}

	public String createOSCommand(String[] args) {
	    return getProxiedModuleInvocation() + " " + StringUtils.getArrayAsStr(args);
    }

	private String executeOSCommand(String cmd) {
	    try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(executeOSCommandInternal(cmd)));
			StringBuffer stdOut = new StringBuffer();
			char[] cbuf = new char[1024];
			for (int read = 0; read > -1; read = rdr.read(cbuf)) stdOut.append(new String(cbuf).substring(0, read));
			return stdOut.toString();
		} catch (IOException e) {
			return generateCLMErrorXML(e);
		}
    }
	
	private InputStream executeOSCommandInternal2(String cmd) {
		try {
	        return Runtime.getRuntime().exec(cmd).getInputStream();
	    } catch (IOException e) {
	    	return null;
	    }
	}

	private InputStream executeOSCommandInternal(String cmd) {
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(cmd.split(" "));
			Map<String, String> env = processBuilder.environment();
			Process proc = processBuilder.start();
			return proc.getInputStream();
	    } catch (IOException e) {
	    	return null;
	    }
	}
	
	public static void main(String[] args) {
		String cmd = "/Users/admin/Slicer3-3.0.2008-02-14-darwin-x86/Slicer3 --launch /Users/admin/Slicer3-3.0.2008-02-14-darwin-x86/lib/Slicer3/Plugins/dwiNoiseFilter /tmp/Slicer3admin/EAFB_vtkMRMLScalarVolumeNodeB.nrrd /tmp/Slicer3admin/EAFB_vtkMRMLScalarVolumeNodeC.nrrd --iter 1 --re 3,3,0 --rf 3,3,0 --mnvf 1 --mnve 1 --minnstd 0 --maxnstd 10000 --hrf 2 --uav";
//		String cmd = "/Users/admin/Slicer3-3.0.2008-02-14-darwin-x86/Slicer3 --launch /Users/admin/Slicer3-3.0.2008-02-14-darwin-x86/lib/Slicer3/Plugins/GradientAnisotropicDiffusion /tmp/Slicer3admin/DGCD_vtkMRMLScalarVolumeNodeC.nrrd /tmp/Slicer3admin/DGCD_vtkMRMLScalarVolumeNodeD.nrrd --conductance 1 --timeStep 0.0625 --iterations 1";
		new WrapperCLMProxyApp(cmd).executeOSCommand(cmd);
	}
}

