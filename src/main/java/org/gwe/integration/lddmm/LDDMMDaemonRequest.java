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

package org.gwe.integration.lddmm;

import java.util.HashMap;
import java.util.Map;

import org.gwe.persistence.model.order.OSCommandDaemonRequest;


/**
 * @author Marco Ruiz
 * @since Sep 27, 2007
 */
public class LDDMMDaemonRequest extends OSCommandDaemonRequest<LDDMMParams> {
	
	private Map<String, String> env;
	
	protected void setup() {
        // Create environment
		env = new HashMap<String, String>();
		env.put("X509_USER_PROXY", createFileUnderWorkspace("/x509up", parameters.getX509up()));
		env.put("LDDMM_HOME",      parameters.getLddmmHome());
		env.put("SERVER_DN",       parameters.getServerDN());
		env.put("AUTH_SCHEME",     parameters.getAuthScheme());
		env.put("mdasDomainName",  parameters.getMdasDomainName());
		env.put("mdasDomainHome",  parameters.getMdasDomainHome());
		env.put("srbHost",         parameters.getSrbHost());
		env.put("srbPort",         parameters.getSrbPort());
		env.put("srbUser",         parameters.getSrbUser());
		env.put("defaultResource", parameters.getDefaultResource());

		String atlas  = getCommandArgument("-A");
        String target = getCommandArgument("-T");
        String outDir = parameters.getDestinationFolder();
		if (outDir == null || "".equals(outDir)) outDir = atlas.substring(0, atlas.lastIndexOf('/') + 1) + "results";
		outDir = outDir.replaceAll("srbfile:", "");
		String atlasName = atlas.substring(atlas.lastIndexOf('/'));
		String resultDir = outDir + atlasName.substring(0, atlasName.indexOf('.'));
        
        StringBuffer pre = new StringBuffer("#!/bin/bash\n\n");
        pre.append("cd ..\n");
        pre.append("chmod -R go=,u=rwX " + workspacePath + "\n\n");
        pre.append(getEnvironmentAsString());
        pre.append("ATLAS=" + atlas + "\n");
        pre.append("TARGET=" + target + "\n");
        pre.append("ATLASNAME=$(basename $ATLAS | perl -pe's/\\..*//')\n");
        pre.append("TARGETNAME=$(basename $TARGET | perl -pe's/\\..*//')\n");
        pre.append("DOMAIN=$(echo $ATLAS | perl -pe's/.*\\/" + parameters.getSrbUser() + "\\.([^-]+-[^\\/]+).*/\\1/')\n");
        pre.append("export srbUser=" + parameters.getSrbUser() + "\n");
        pre.append("export mdasDomainName=$DOMAIN\n");
        pre.append("export mdasDomainHome=$DOMAIN\n");
        pre.append("export SRB_HOME=/usr/local/bin/srb34/\n");
        pre.append("Smkdir " + outDir + "\n");
        pre.append("Smkdir " + resultDir + "\n");
        pre.append("printenv\n\n");
 
        String configPath = createFileUnderWorkspace("/lddmm-config.txt", parameters.getLddmmConfig());
        pre.append(localizedCmd + " -o srbdir:" + resultDir + " -m -c " + configPath + "\n");

        localizedCmd = "sh " + createFileUnderWorkspace("/job.sh", pre.toString());
	}

	protected Map<String, String> getEnvironment() { return env; }
	
	private String getEnvironmentAsString() {
		String result = "";
		for (String key : env.keySet()) result += "export " + key + "=" + env.get(key) + "\n";
		return result;
	}

	protected void tearDown() {
		
	}
	
    // Should never return "null", as this could cause something along the lines of cat | foo, instead of 
	// cat blah | foo; one is an infinite loop, the other is just an error.
    private String getCommandArgument(String argType) {
        String[] args = localizedCmd.split("\\s+");
        for(int idx = 0; idx < args.length; idx++) 
            if (argType.equals(args[idx])) return args[++idx];

        return "undefined";
    }
}
