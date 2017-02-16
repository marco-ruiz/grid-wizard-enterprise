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

package org.gwe.drivers.netAccess;

/**
 * @author Neil Jones
 * @since Feb 5, 2007
 */
public class ConnectorException extends Exception {
	
	private String command = null;
	
    public ConnectorException(String msg, Exception nestee) {
        super(msg, nestee);
    }

	public ConnectorException(String msg) {
		super(msg);
    }
	
	public String getCommand() {
    	return command;
    }

	public void setCommand(String command) {
    	this.command = command;
    }

	public String getMessage() {
		String prefix = (command == null) ? "" : "Command '" + command + "' failed to execute with the following exception:\n";
		return prefix + super.getMessage();
	}
}