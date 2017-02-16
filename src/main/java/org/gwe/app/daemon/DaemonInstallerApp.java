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

package org.gwe.app.daemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.persistence.model.DaemonInstallation;

/**
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class DaemonInstallerApp {

	private static Log log = LogFactory.getLog(DaemonInstallerApp.class);

	public static final String SPRING_INSTALLER_CONF = "spring-gwe-installer.xml";

	public static void main(String[] args) {
		new DaemonInstallerApp(args[0]);
	}

	public DaemonInstallerApp(String installPath) {
        DaemonInstallation installation = new DaemonInstallation(installPath);
		installation.createDirectories();
		new GWEAppContext(DaemonInstallerApp.class, installPath, installation.getInstallationWorkspacePath(), DaemonInstallerApp.SPRING_INSTALLER_CONF);
    }
}

