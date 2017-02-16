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

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.RemoteHostHandle;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

/**
 * @author Marco Ruiz
 * @since Dec 7, 2008
 */
public class HostTunnel {
	
	private static Log log = LogFactory.getLog(HostTunnel.class);

	public static final int NO_TUNNEL_PORT = -1;

	// Remote to local ports map
	private int localPort = NO_TUNNEL_PORT; 
	
	private ResourceLink<? extends RemoteHostHandle> hostLink;
	private RemoteHostHandle hostHandle;
	
	public <HANDLE_TYPE> HostTunnel(ResourceLink<? extends HostHandle> link) {
		if (link != null && !RemoteHostHandle.class.isAssignableFrom(link.getHandleClass())) link = null;
	    this.hostLink = (ResourceLink<? extends RemoteHostHandle>) link;
    }
	
	public int openTunnel(ThinURI hostURI, int remotePort, boolean force) throws HostTunnelException {
		if (hostLink == null) return remotePort;
		if (force) disposeHandle();

		if (hostHandle == null) resetTunnel(remotePort);  
		return getTunnelPort(remotePort);
	}

	private void disposeHandle() {
		if (hostHandle == null) return;
        try {
	        hostHandle.close();
        } catch (HandleOperationException e) {
	        // TODO Auto-generated catch block
        }
	    hostHandle = null;
    }

	private void resetTunnel(int remotePort) throws HostTunnelException {
		localPort = NO_TUNNEL_PORT;
	    try {
	    	hostHandle = hostLink.createHandle();
	    } catch (HandleCreationException e) {
			throw new HostTunnelException("Problems creating host handle to tunnel port " + remotePort + " for link '" + hostLink + "'", e);
	    }
    }

	private int getTunnelPort(int remotePort) throws HostTunnelException {
		if (localPort != NO_TUNNEL_PORT) return localPort;
	    try {
		    localPort = findFreePort();
		    hostHandle.openTunnel(localPort, remotePort);
		    return localPort;
		} catch (IOException e) {
			throw new HostTunnelException("Could not find a local free port to establish tunnel for link '" + hostLink + "'", e);
	    } catch (HandleOperationException e) {
			throw new HostTunnelException("Problems establishing tunnel to port " + remotePort + " for link '" + hostLink + "'", e);
	    }
    }
	
	private int findFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		return port;
	}
}


