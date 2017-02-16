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

import java.io.File;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Sep 27, 2007
 */
public class LDDMMParams implements Serializable {
	
    private static Log log = LogFactory.getLog(LDDMMParams.class);

	private String x509up;
	private String lddmmConfig;
	private String lddmmHome;
	private String serverDN;
	private String authScheme;
	private String mdasDomainName;
	private String mdasDomainHome;
	private String srbHost;
	private String srbPort;
	private String srbUser;
	private String defaultResource;
	private String destinationFolder;
	
	public String getX509up() {
		return x509up;
	}
	
	public void setX509up(String x509up) {
		this.x509up = x509up;
	}

	public void setX509upFile(String filename) {
		setX509up(getFileContents(filename));
	}

	public String getLddmmConfig() {
		return lddmmConfig;
	}
	
	public void setLddmmConfig(String lddmmConfig) {
		this.lddmmConfig = lddmmConfig;
	}
	
	public void setLddmmConfigFile(String filename) {
		setLddmmConfig(getFileContents(filename));
	}
	
	public String getFileContents(String filename) {
		try {
			return IOUtils.readFile(new File(filename));
		} catch (Exception e) {
			log.warn("Could read contents of file '" + filename + "'", e);
		}
		return "";
	}
	
	public String getLddmmHome() {
		return lddmmHome;
	}
	
	public void setLddmmHome(String lddmmHome) {
		this.lddmmHome = lddmmHome;
	}
	
	public String getServerDN() {
		return serverDN;
	}
	
	public void setServerDN(String serverDN) {
		this.serverDN = serverDN;
	}
	
	public String getAuthScheme() {
		return authScheme;
	}
	
	public void setAuthScheme(String authScheme) {
		this.authScheme = authScheme;
	}
	
	public String getMdasDomainName() {
		return mdasDomainName;
	}
	
	public void setMdasDomainName(String mdasDomainName) {
		this.mdasDomainName = mdasDomainName;
	}
	
	public String getMdasDomainHome() {
		return mdasDomainHome;
	}
	
	public void setMdasDomainHome(String mdasDomainHome) {
		this.mdasDomainHome = mdasDomainHome;
	}
	public String getSrbHost() {
		return srbHost;
	}
	
	public void setSrbHost(String srbHost) {
		this.srbHost = srbHost;
	}
	
	public String getSrbPort() {
		return srbPort;
	}
	
	public void setSrbPort(String srbPort) {
		this.srbPort = srbPort;
	}
	
	public String getSrbUser() {
		return srbUser;
	}

	public void setSrbUser(String srbUser) {
		this.srbUser = srbUser;
	}

	public String getDefaultResource() {
		return defaultResource;
	}
	
	public void setDefaultResource(String defaultResource) {
		this.defaultResource = defaultResource;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}
}
