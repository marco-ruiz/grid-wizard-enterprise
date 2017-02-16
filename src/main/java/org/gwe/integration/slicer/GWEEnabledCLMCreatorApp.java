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

import java.io.File;
import java.io.IOException;

import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2007
 */
public class GWEEnabledCLMCreatorApp {

	private static final String SCRIPT_PREFFIX = "gweCLMP-";

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String pluginsDir = System.getenv(AbstractCLMProxyApp.ENV_SLICER_HOME) + "/lib/Slicer3/Plugins";
		
		File pluginsDirFile = new File(pluginsDir);
		
		// Clean up any previous installation for the same version
	    for (File file : pluginsDirFile.listFiles()) 
	    	if (file.getName().startsWith(SCRIPT_PREFFIX)) file.delete();
	    
	    // Install new CLM proxies
		String scriptPrefix = createScriptPrefix();
	    for (File file : pluginsDirFile.listFiles()) {
	    	if (file.isFile() && !file.getName().startsWith("gwe") && !file.getName().endsWith(".log")) {
	    		System.out.print("Generating CLMP for " + file.getName() + "...");
	    		String filename = IOUtils.concatenatePaths(pluginsDir, SCRIPT_PREFFIX + file.getName() + ".sh");
				File clmGWEProxy = new File(filename);
				try {
					IOUtils.createLocalExecutableFile(clmGWEProxy.getAbsolutePath(), scriptPrefix + file.getName() + " $@\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
	    		System.out.println("... Done!");
	    	}
		}
	}
	
	private static String createScriptPrefix() {
	    String scriptPreffix = "#!/bin/sh\n";
		scriptPreffix += "# Script to proxy into a more real slicer module\n\n"; 
//		scriptPreffix += "if [ ! -d $GWE_HOME ]; then\n";
		scriptPreffix += "export GWE_HOME=`ls -d -t $" + AbstractCLMProxyApp.ENV_SLICER_HOME + "/gwe-*/ | head -1`\n"; 
//		scriptPreffix += "fi;\n";
//		scriptPreffix += "export GWE_FAT_JAR=`ls $GWE_HOME/lib/gwe-*-fatjar.jar`\n";
//		scriptPreffix += "export GWE_CORE_JAR=`ls $GWE_HOME/lib/gwe-*-core.jar`\n"; 
//		scriptPreffix += "export GWE_DEP_JAR=`ls $GWE_HOME/lib/gwe-*-dep.jar`\n"; 
//		scriptPreffix += "java -Xmx512m -cp $GWE_HOME/conf:$GWE_CORE_JAR:$GWE_DEP_JAR " + GWECLMProxyApp.class.getName() + " ";
		scriptPreffix += "$GWE_HOME/bin/gwe-base-script.sh " + GWECLMProxyApp.class.getName() + " ";
	    return scriptPreffix;
    }
}

