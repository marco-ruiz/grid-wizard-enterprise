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

package org.gwe.drivers.netAccess.handles;

import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.security.ResourceLink;

/**
 * @author Marco Ruiz
 * @since Sep 17, 2008
 */
public class LocalHostHandle extends HostHandle {

    public LocalHostHandle(ResourceLink link) {
	    super(link);
    }

	public String runCommand(ShellCommand command) throws ConnectorException {
	    return command.runLocally();
    }
}
