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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;


/**
 * @author Marco Ruiz
 * @since Dec 7, 2008
 */
public class HostTunnelsRepo {
	
	private static Log log = LogFactory.getLog(HostTunnelsRepo.class);

	private Map<HostTunnelTarget, HostTunnel> repo = new HashMap<HostTunnelTarget, HostTunnel> ();
	private KeyStore keys;
	
	public HostTunnelsRepo(KeyStore keys) {
		this.keys = keys;
    }

	public int openTunnel(ProtocolScheme scheme, String host, int remotePort, boolean force) throws HostTunnelException {
		HostTunnelTarget target = new HostTunnelTarget(scheme, host, remotePort);
		if (target.isLocalHost()) return HostTunnel.NO_TUNNEL_PORT;
		HostTunnel tunnel = repo.get(target);
		ThinURI targetURI = target.getHostURI();
		if (tunnel == null) {
			ResourceLink<HostHandle> hostLink = keys.createHostLink(targetURI.toString());
			tunnel = new HostTunnel(hostLink);
			repo.put(target, tunnel);
		}
		int localPort = tunnel.openTunnel(targetURI, remotePort, force);
		log.trace("Request to open tunnel to the remote port " + remotePort + " of " + targetURI + " (force=" + force + ") did so on local port " + localPort);
		return localPort;
	}
}
