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

package org.gwe.persistence.model;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Marco Ruiz
 * @since Aug 8, 2007
 */
@Entity
public class ComputeResourceInfo extends BaseModelInfo<String> {

	public static ComputeResourceInfo createLocalInfo(int allocationId) {
		String hostAddress = "UNKNOWN-FOR-ALLOCATION-ID-" + allocationId;
		String hostName    = hostAddress;
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			hostAddress = localhost.getHostAddress();
			hostName    = localhost.getHostName();
		} catch (UnknownHostException e) {
		}
		
		return new ComputeResourceInfo(
				hostAddress,
				hostName,
				System.getProperty("java.version"),
				Runtime.getRuntime().availableProcessors(),
				System.getProperty("os.arch"),
				System.getProperty("os.name"),
				System.getProperty("os.version")
		);
	}
	
	@Id
	private String hostAddress;
	private String hostName;
	
	private String javaVersion;
	private String osName;
	private String osVersion; 
	private int osProcessors; 
	private String osArch;
	
	public ComputeResourceInfo(String hostAddress, String hostName, String javaVersion, int osProcessors, String osArch, String osName, String osVersion) {
		this.hostAddress = hostAddress;
		this.hostName = hostName;
		this.javaVersion = javaVersion;
		this.osProcessors = osProcessors;
		this.osArch = osArch;
		this.osName = osName;
		this.osVersion = osVersion;
	}

	public ComputeResourceInfo() {}

	public String getId() 			{ return hostAddress; }
	public String getHostAddress() 	{ return hostAddress; }
	public String getHostName() 	{ return hostName; }
	public String getJavaVersion() 	{ return javaVersion; }
	public int getOsProcessors() 	{ return osProcessors; }
	public String getOsArch() 		{ return osArch; }
	public String getOsName() 		{ return osName; }
	public String getOsVersion() 	{ return osVersion; }

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((hostAddress == null) ? 0 : hostAddress.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    ComputeResourceInfo other = (ComputeResourceInfo) obj;
	    if (hostAddress == null) {
		    if (other.hostAddress != null)
			    return false;
	    } else if (!hostAddress.equals(other.hostAddress))
		    return false;
	    return true;
    }
}
