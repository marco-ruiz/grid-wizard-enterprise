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

package org.gwe.app.client;

import java.io.IOException;

import jline.ConsoleReader;

/**
 * @author Marco Ruiz
 * @since Jan 17, 2009
 */
public class GWEConsoleReader extends ConsoleReader {

	private ProgressTracker tracker;
	
	public GWEConsoleReader(ProgressTracker tracker) throws IOException {
		this.tracker = tracker;
    }
	
	public String readMultiLines(String prompt, String escapeOK, String escapeCancel) throws IOException {
	    String multiLines = "";
    	boolean lastMultiLine = false;
		while (!lastMultiLine) {
			String line = readLine();
			line = line.trim();
			if (line.endsWith(escapeCancel)) return "";
			if (line.endsWith(escapeOK)) {
				line = line.substring(0, line.length() - escapeOK.length());
				lastMultiLine = true;
			}
			multiLines += " " + line;
    	}
		return multiLines;
    }

	public String readPasskey(String prompt, boolean passwordConfirmation) throws IOException {
	    boolean passwordCaptured = false;
	    String valueRead = null;
	    while (!passwordCaptured) {
	    	valueRead = readLine(prompt, '*');
	        String confirmation = passwordConfirmation ? readLine("confirm " + prompt, '*') : valueRead;
	        passwordCaptured = confirmation.equals(valueRead); 
	        if (!passwordCaptured)
                tracker.trackProgress("Pass keys do not match!");
	    }
	    return valueRead;
    }
	
	public boolean readBoolean(String prompt) {
	    tracker.trackProgress(prompt + " (Y/N): ");
	    
	    try {
	        int character = readCharacter(new char[] {'y', 'Y', 'n', 'N'});
	        return (character == 'y' || character == 'Y');
	    } catch (IOException e) {
	    	return false;
	    }
    }
}
