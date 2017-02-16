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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jline.ArgumentCompletor;
import jline.CandidateListCompletionHandler;
import jline.Completor;
import jline.ConsoleReader;
import jline.History;
import jline.NullCompletor;
import jline.SimpleCompletor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.app.client.GWEConsoleReader;
import org.gwe.app.client.ProgressTracker;
import org.gwe.utils.StringUtils;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.security.CredentialNotFoundException;

/**
 * @author Marco Ruiz
 * @since Dec 10, 2007
 */
public class TerminalShellApp extends ClientShellApp {

	private static Log log = LogFactory.getLog(TerminalShellApp.class);

	public static void main(String[] args) throws IOException, CredentialNotFoundException {
		try {
			TerminalShellApp app = new TerminalShellApp(0, new ArgsList(args));
			app.runTerminal();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
//			println("\tCould not connect to daemon at " + appConfig.getConnectionURL() + ". ");
		}
		System.exit(0);
	}
	
	public TerminalShellApp(int index, ArgsList argsList) throws RemoteException, CredentialNotFoundException, IOException {
		super("Terminal", ProgressTracker.CONSOLE_TRACKER, index, argsList);
	}

	public void runTerminal() throws IOException {
		String connURL = getSession().getSession().getDaemonConfig().getConnectionURL();
		ConsoleReader consoleReader = new ConsoleReader();
    	consoleReader.addCompletor(new ArgumentCompletor (new Completor[] {
    			new SimpleCompletor (getCommandList().toArray(new String[]{})),
    			new NullCompletor()
    	}));
    	
    	((CandidateListCompletionHandler)consoleReader.getCompletionHandler()).setAlwaysIncludeNewline(false);
    	// History
    	try {
			String historyFileName = appConfig.getInstallFiles().getFilePath(".terminal_history");
			consoleReader.setHistory(new History(new File(historyFileName)));
    	} catch(Exception e) {}

    	while (true) {
			String line = consoleReader.readLine("GWE [" + connURL + "] > ");
            if (line == null) return;
            if (QUEUE_ORDER_COMMAND.equals(line.trim())) line = QUEUE_ORDER_COMMAND + " " + readMultiLines("<OK>", "<CANCEL>");
            
			String[] cmd = StringUtils.splitSpaceSeparated(line);
            if (cmd.length == 0) continue;

            String cmdName = cmd[0];
            if (cmdName.equalsIgnoreCase("exit") || cmdName.equalsIgnoreCase("quit")) {
                tracker.trackProgress("Disconnecting...\nBye!");
            	return;
            }

            try {
           		tracker.trackProgress(processCommand(cmd));
            } catch (Exception e) {
            	log.info("Command " + line + " failed with exception ... ", e);
                tracker.trackProgress("Command failed: [" + e.getMessage() + "]\n");
            }
        }
    }
	
	private String readMultiLines(String escapeOK, String escapeCancel) throws IOException {
		printEditorTitle(escapeOK, escapeCancel); 

		List<String> completions = new ArrayList<String>();
		for (String p2elFunction : GWEAppContext.getP2ELFunctionNames()) completions.add("$" + p2elFunction + "()");
		completions.add("${VAR_}");
		completions.add(escapeOK);
		completions.add(escapeCancel);
		
		GWEConsoleReader consoleReader = new GWEConsoleReader(tracker);
/*
    	consoleReader.addCompletor(new ArgumentCompletor (new Completor[] {
    			new SimpleCompletor (completions.toArray(new String[]{})),
    			new NullCompletor()
    	}));
*/    	
    	((CandidateListCompletionHandler)consoleReader.getCompletionHandler()).setAlwaysIncludeNewline(false);

    	return consoleReader.readMultiLines("P2EL > ", escapeOK, escapeCancel);
    }

	private void printEditorTitle(String escapeOK, String escapeCancel) {
	    String editorTitle = " P2EL Editor (escape with '" + escapeOK + "' or '" + escapeCancel + "' at the end of your P2EL statement)";
		byte[] lineBytes = new byte[editorTitle.length() + 2];
		Arrays.fill(lineBytes, (byte)'-');
		String lineStr = new String(lineBytes);
		tracker.trackProgress(lineStr + "\n" + editorTitle + "\n" + lineStr);
    }
}

