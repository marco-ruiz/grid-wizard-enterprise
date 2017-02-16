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

package org.gwe.persistence.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.DaemonInstallation;
import org.gwe.persistence.model.OSAppFolder;
import org.gwe.utils.IOUtils;


/**
 * @author Marco Ruiz
 * @since Aug 13, 2007
 */
public class DaemonConfigDescDAO {
	
	private DaemonInstallation installation;
	private boolean databaseScheduledForCreationOnThisRun;
	private DaemonConfigDesc configuration;
	
	public DaemonConfigDescDAO() {}
	
	public DaemonConfigDescDAO(String installPath) throws FileNotFoundException, IOException, ClassNotFoundException {
		setInstallation(new DaemonInstallation(installPath));
    }

	public void setInstallPath(String installPath) throws FileNotFoundException, IOException, ClassNotFoundException {
		setInstallation(new DaemonInstallation(installPath));
    }

	public void setInstallation(DaemonInstallation installation) throws FileNotFoundException, IOException, ClassNotFoundException {
		this.installation = installation;
		this.configuration = IOUtils.deserializeObject(new FileInputStream(installation.getConfigurationFilePath()));
		this.databaseScheduledForCreationOnThisRun = !new File(getDatabaseFilePath()).exists();
    }

	public boolean isDatabaseScheduledForCreationOnThisRun() {
    	return databaseScheduledForCreationOnThisRun;
    }

	public DaemonConfigDesc getConfiguration() throws IOException, ClassNotFoundException {
	    return configuration;
    }
	
	public String getDatabaseURL() {
        String derbyLog = IOUtils.concatenatePaths(installation.getWorkspacePath(), "derby.log");
        return "jdbc:derby:" + getDatabaseFilePath() + ";create=" + databaseScheduledForCreationOnThisRun; // + ";logDevice=" + derbyLog;
	}

	public String getDatabaseFilePath() {
//	    String dbRoot = OSAppFolder.DATABASE.getRelativeTo(installation.getInstallPath());
	    String dbRoot = OSAppFolder.DATABASE.getRelativeTo(configuration.getHeadResource().getDatabasePath());
		return IOUtils.concatenatePaths(dbRoot, "daemon");
    }

}

