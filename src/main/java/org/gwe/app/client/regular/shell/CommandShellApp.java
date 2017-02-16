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

package org.gwe.app.client.regular.shell;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.client.ProgressTracker;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.security.CredentialNotFoundException;

/**
 * @author Marco Ruiz
 * @since Dec 10, 2007
 */
public class CommandShellApp extends ClientShellApp {

	private static Log log = LogFactory.getLog(CommandShellApp.class);

	public static void main(String[] args) {
		// First parameter is the "command id" then it comes the optional "configuration argument"
		// Done this way to allow the script to hardcode the "command id" and left the rest for further processing
		// Including the optional "configuration argument"
		ArgsList argsList = new ArgsList(args);
        try {
    		CommandShellApp app = new CommandShellApp(1, argsList);
			String cmdResult = app.processCommand(argsList.getArgs());
			System.out.println(cmdResult);
			System.exit(0);
        } catch (Exception e) {
        	log.error("Problems executing command '" + argsList.toString() + "'", e);
	        e.printStackTrace();
			System.exit(1);
        }
	}
	
	public CommandShellApp(int index, ArgsList argsList) throws CredentialNotFoundException, PasswordMismatchException, IOException { 
		super("Command Shell", ProgressTracker.LOG_TRACKER, index, argsList); 
	}
}
