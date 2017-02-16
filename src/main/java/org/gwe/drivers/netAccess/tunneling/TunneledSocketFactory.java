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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.ProtocolScheme;

/**
 * @author Marco Ruiz
 * @since Aug 16, 2007
 */
public class TunneledSocketFactory implements RMIClientSocketFactory, Serializable {

	private static Log log = LogFactory.getLog(TunneledSocketFactory.class);

    private static HostTunnelsRepo tunnelsRepo = null;
	
	public static void setKeys(KeyStore keys) {
		// Semi final property (only set once!)
		if (tunnelsRepo == null) {
			tunnelsRepo = new HostTunnelsRepo(keys);
			log.info("Tunnels repository set!");
		}
    }

    private String serverAddress;
	
	public TunneledSocketFactory() { 
		this("127.0.0.1");
	}
	
	public TunneledSocketFactory(String serverAddress) { 
		this.serverAddress = serverAddress; 
	}
	
	public Socket createSocket(String host, int port) throws IOException {
		try {
			return createSocketToTunnelFor(host, port, false);
		} catch(java.net.ConnectException e) { // This is the exception thrown when the server is unreachable
			return createSocketToTunnelFor(host, port, true);
		}
    }

	private Socket createSocketToTunnelFor(String host, int remotePort, boolean force) throws IOException {
		int port = HostTunnel.NO_TUNNEL_PORT;
        try {
        	if (tunnelsRepo != null) 
        		port = tunnelsRepo.openTunnel(ProtocolScheme.SSH, host, remotePort, force);
        } catch (HostTunnelException e) {
        	log.error(e);
        	throw new IOException(e.getMessage());
        } catch (Exception e) {
        	// Ignore
        	log.warn("Problems opening tunnel to ssh://" + host + ":" + remotePort, e);
        }

		log.trace("Tunnel defined as 'localhost:" + port + "' (translate from '" + host + ":" + remotePort + "')");
		return (port != HostTunnel.NO_TUNNEL_PORT) ? createSocketForReal(serverAddress, port) : createSocketForReal(host, remotePort); 
	}

	private Socket createSocketForReal(String host, int port) throws UnknownHostException, IOException {
        log.trace("Socket factory received request to create socket for real to '" + host + ":" + port + "'" );
	    Socket socket = new Socket(Proxy.NO_PROXY);
        InetAddress hostAddress = InetAddress.getByName(host);
		InetSocketAddress inetSocketAddress = new InetSocketAddress(hostAddress, port);
        log.trace("RMI socket factory will try to connect to '" + inetSocketAddress + "'");
        socket.connect(inetSocketAddress);
        log.trace("RMI socket connected to '" + inetSocketAddress + "'!");
        return socket;
    }
}
