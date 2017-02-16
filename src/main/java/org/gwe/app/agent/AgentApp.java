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

package org.gwe.app.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.api.ServerAPI4Agent;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.DaemonInstallation;

/**
 * @author Marco Ruiz
 * @since Jul 6, 2007
 */
public class AgentApp {

	private static Log log = LogFactory.getLog(AgentApp.class);
	public static final String SPRING_AGENT_CONF = "spring-gwe-agent.xml";

    public static void main(String[] args) {
    	try {
    		String daemonInstallPath = args[0];
			int allocId = Integer.parseInt(args[1]);
			DaemonConfigDesc config = initContext(daemonInstallPath, allocId);
			config.initializeServices();
			ServerAPI4Agent agentAPI = config.createAPILink().createAPIProxy(ServerAPI4Agent.class);
//			log.info("Main invoked with parameters: " + parameters);
			BaseAgent agent = new BaseAgent(agentAPI, config, allocId);
			agent.call();
    	} catch (Exception e) {
	    	log.fatal("Non-recoverable exception occured in agent. Exiting!", e);
			exit();
		}
		
		log.info("Bye!");
		exit();
    }

	private static DaemonConfigDesc initContext(String daemonInstallPath, int allocId) {
	    DaemonInstallation install = new DaemonInstallation(daemonInstallPath);
		GWEAppContext ctx = new GWEAppContext(AgentApp.class, daemonInstallPath, install.getAllocsWorkspacePath(allocId), SPRING_AGENT_CONF);
		return ctx.getBeanOfClass(DaemonConfigDesc.class);
    }

	private static void exit() {
		// TODO: Issue GWE-9. Clean up workspace and backup log file to daemon's file database
//		alloc.getWorkspacePath();
		System.exit(0);
	}
}

