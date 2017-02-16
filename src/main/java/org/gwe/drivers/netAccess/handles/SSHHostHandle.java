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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ExecutionChannelInspector;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.RemoteHostHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.security.ResourceLink;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;

/**
 * @author Marco Ruiz
 * @since Sep 7, 2008
 */
public class SSHHostHandle extends RemoteHostHandle {

    private static Log log = LogFactory.getLog(SSHHostHandle.class);

	private JSchConnection conn;
    	
    public SSHHostHandle(ResourceLink<HostHandle> link) throws HandleCreationException {
    	super(link);
    	try {
    		conn = new JSchConnection(link);
    	} catch (Exception ex) {
            log.info("Connection failed: aborting", ex);
            throw new HandleCreationException("Jsch could not established a connection.", ex);
        }
    }
    
	public void openTunnel(int localPort, int remotePort) throws HandleOperationException {
		try {
			// Try to clean it up if it exists
			conn.getSessionObj().delPortForwardingL(localPort);
		} catch (JSchException e1) {
			// Never mind. Maybe it doesn't exist yet
		}

		try {
			conn.getSessionObj().setPortForwardingL(localPort, link.getURI().getHost(), remotePort);
			log.info("SSH tunnel created = [localhost:" + localPort + "] >==< [" + link.getURI().getHost() + ":" + remotePort +"]");
		} catch (JSchException e) {
//			log.debug("Exception thrown while trying to establish tunnel " + tunnel, e);
//			throw new HandleCreationException("Could not open a proxy tunnel", e);
		}
	}
    
	public String runCommand(ShellCommand cmd) throws ConnectorException {
		ChannelExec channel = null;
        try {
        	//
            log.debug("Opening execution channel to " + link.getURI());
            channel = (ChannelExec)conn.getSessionObj().openChannel("exec");
    		channel.setCommand(cmd.getUnixStyleCmd());
    		channel.setErrStream(System.err);
    		InputStream resultStream = channel.getInputStream();
    		channel.connect(30000);
    		log.debug("Channel successfully connected");
    		String res = readExecutionOutput(cmd, channel, resultStream);
            log.debug("Command results:\n" + res);
            return res;
        } catch (JSchException e) {
            throw new ConnectorException("Error encountered while creating SSH channel to connect to " + link.getURI(), e);
        } catch (IOException e) {
            throw new ConnectorException("Error executing command " + cmd.getCmd(), e);
        } finally {
    		if (channel != null) channel.disconnect();
        }
	}

	private String readExecutionOutput(ShellCommand cmd, final ChannelExec channel, InputStream resultStream) throws IOException {
		ExecutionChannelInspector inspector = new ExecutionChannelInspector() {
			public boolean isExecutionChannelOpened() {
			    if (channel.isClosed()) {
			        log.info("Exit status: " + channel.getExitStatus());
			        return false;
			    }
			    try {
			        Thread.yield();
			    } catch (Exception ex) {} // From the Exec example in jsch.
		        return true;
		    }
		};
		return cmd.readExecutionOutput(resultStream, inspector);
	}
	
	public void close() throws HandleOperationException {
		try {
	        conn.close();
        } catch (ConnectorException e) {
        	log.warn(e);
        	throw new HandleOperationException(e);
        }
        conn = null;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} finally {
			super.finalize();
		}
	}
}
