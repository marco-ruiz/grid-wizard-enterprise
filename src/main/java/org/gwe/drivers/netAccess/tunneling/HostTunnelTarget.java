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

package org.gwe.drivers.netAccess.tunneling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ThinURI;

/**
 * @author Marco Ruiz
 * @since Feb 10, 2009
 */
class HostTunnelTarget {

	private static Map<InetAddress, String> hostNames = new HashMap<InetAddress, String> ();

	private ThinURI hostURI;
	private ProtocolScheme scheme;
    private InetAddress hostAddress;
	private int remotePort;
	private boolean isLocalHost;
	
	public HostTunnelTarget(ProtocolScheme scheme, String host, int remotePort) throws HostTunnelException {
	    this.scheme = scheme;
	    this.remotePort = remotePort;
	    try {
		    this.hostAddress = InetAddress.getByName(host);
			isLocalHost = hostAddress.equals(getLocalHostAddress()) || hostAddress.isAnyLocalAddress() || hostAddress.isLinkLocalAddress() || hostAddress.isLoopbackAddress();
			if (!isLocalHost) {
				registerHostName(host);
			    this.hostURI = new ThinURI(scheme, hostNames.get(hostAddress), "");
			}
	    } catch (UnknownHostException e) {
	    	throw new HostTunnelException("Could not find the IP address of host '" + host + "'", e);
	    }
    }
	
	private InetAddress getLocalHostAddress() throws UnknownHostException {
	    return InetAddress.getLocalHost();
    }
	
	private void registerHostName(String host) {
	    if (hostNames.get(hostAddress) == null) 
	    	hostNames.put(hostAddress, host);
    }

	public boolean isLocalHost() {
	    return isLocalHost;
    }

	public ThinURI getHostURI() {
    	return hostURI;
    }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hostAddress == null) ? 0 : hostAddress.hashCode());
		result = prime * result + remotePort;
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
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
		HostTunnelTarget other = (HostTunnelTarget) obj;
		if (hostAddress == null) {
			if (other.hostAddress != null)
				return false;
		} else if (!hostAddress.equals(other.hostAddress))
			return false;
		if (remotePort != other.remotePort)
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		return true;
	}
}
