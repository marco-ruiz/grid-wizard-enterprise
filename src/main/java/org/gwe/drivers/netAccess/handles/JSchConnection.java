/*
 * Copyright 2007-2008 the original author or authors. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.gwe.drivers.netAccess.handles;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.utils.security.AccessControl;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.Realm;
import org.gwe.utils.security.ResourceLink;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author Marco Ruiz
 * @since May 16, 2008
 */
public class JSchConnection {
	
	private static Log log = LogFactory.getLog(JSchConnection.class);
	
	public static List<Exception> test(KeyStore keys) {
		List<Exception> result = new ArrayList<Exception>(); 
		for (AccessControl ac : keys.getAccessControls()) {
			for (Realm realm : ac.getRealms()) {
				try {
//		            new JSchConnection(new SSHHostLink(realm.getTestHost(), ac.getAccount())).close();
	            } catch (Exception e) {
	            	result.add(e);
	            }
            }
        }
		return result;
	}

	private ResourceLink<HostHandle> link;
	private JSch jschObj;
	private Session sessionObj;
	
	public JSchConnection(ResourceLink<HostHandle> link) throws JSchException, UnknownHostException {
		this.link = link;
		AccountInfo acct = link.getAccountInfo();
		log.debug("Creating ssh connector for " + acct.getUser() + "@" + link.getURI());
		
		jschObj = createJSchObject(acct.getPrivateKey(), acct.getPublicKey(), acct.getPassphrase());
		createJSchSession();
	}
	
	private JSch createJSchObject(byte[] privateKey, byte[] publicKey, String passphrase) throws JSchException {
		JSch result = new JSch();
		// result.setKnownHosts(link.getAccountInfo().getHomeDir() + "/.ssh/known_hosts");
		result.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
		if (privateKey != null) result.addIdentity("byte-array", privateKey, publicKey, passphrase.getBytes());
		return result;
	}

	private void createJSchSession() throws JSchException, UnknownHostException {
		AccountInfo acct = link.getAccountInfo();
		String host = (link != null && link.getURI() != null) ? link.getURI().getHost() : InetAddress.getLocalHost().getHostName();
		log.info("Connecting to " + acct.getUser() + "@" + host);
		sessionObj = jschObj.getSession(acct.getUser(), host);
		
		// Set SSH configuration
        Properties config = new Properties();
        config.setProperty("StrictHostKeyChecking", "ask");
		sessionObj.setConfig(config);

//		sessionObj.setDaemonThread(true);
//		if (acct.getPassword() != null) 
		sessionObj.setUserInfo(new JSchUserInfo(acct));
		sessionObj.connect(30000);
		log.info("Successfully connected");
	}

	public JSch getJschObj() {
    	return jschObj;
    }

	public Session getSessionObj() {
    	return sessionObj;
    }

	public void close() throws ConnectorException {
        if (sessionObj != null) {
        	log.info("Closing SSH connection to " + link);
        	sessionObj.disconnect();
        	sessionObj = null;
        }
	}

	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
