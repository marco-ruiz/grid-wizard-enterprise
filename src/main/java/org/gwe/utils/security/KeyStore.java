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

package org.gwe.utils.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.ResourceHandle;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.netAccess.HostHandle;

/**
 * @author Marco Ruiz
 * @since Jul 12, 2007
 */
public class KeyStore implements Serializable {
	
	private static Log log = LogFactory.getLog(KeyStore.class);

	private static final List<AccessControl> DEFAULT_ACCESS_CONTROLS;
	
	static {
		DEFAULT_ACCESS_CONTROLS = new ArrayList<AccessControl>();
		DEFAULT_ACCESS_CONTROLS.add(AccessControl.createDefaultAC(ProtocolScheme.LOCAL, "localhost"));
		DEFAULT_ACCESS_CONTROLS.add(AccessControl.createDefaultAC(ProtocolScheme.FILE, ""));
	}
	
	public static KeyStore createKeyStore(AccountInfo account, String host) {
		// Create realm
        Realm realm = new Realm(ProtocolScheme.SSH + ";" + ProtocolScheme.SFTP, host, host);
        
        // Create access control
        List<AccessControl> acs = new ArrayList<AccessControl>();
		acs.add(new AccessControl(account, realm));
        
        // Create keystore
        KeyStore result = new KeyStore();
        result.setAccessControls(acs);
        return result;
	}
	
//	private List<KeyStoreEntry> entries = new ArrayList<KeyStoreEntry>();
	private List<AccessControl> accessControls = new ArrayList<AccessControl>();

	public List<AccessControl> getAccessControls() {
		if (accessControls == null)
			accessControls = new ArrayList<AccessControl>();
    	return accessControls;
    }

	public void setAccessControls(List<AccessControl> accessControls) {
    	this.accessControls = accessControls;
    }

	public void init() {
	    for (AccessControl currAC : getAccessControls()) {
	    	AccountInfo acct = currAC.getAccount();
			acct.init();
			for (Realm realm : currAC.getRealms()) realm.setAccount(acct);
	    }
    }
	
	public List<Realm> getRealms() {
		List<Realm> realms = new ArrayList<Realm>();
		
		for (AccessControl ac : getAccessControls()) {
	        for (Realm realm : ac.getRealms()) realms.add(realm);
	    }
		return realms;
	}
	
	public Realm resolveRealm(ProtocolScheme scheme, String host) {
		Realm matchingRealm = resolveRealm(scheme, host, getAccessControls());
		return (matchingRealm != null) ? matchingRealm : resolveRealm(scheme, host, DEFAULT_ACCESS_CONTROLS);
    }

	private Realm resolveRealm(ProtocolScheme scheme, String host, List<AccessControl> acList) {
	    Realm matchingRealm;
	    for (AccessControl currAC : acList) {
	    	matchingRealm = currAC.findMatchingRealm(scheme, host);
			if (matchingRealm != null) return matchingRealm;
	    }
		return null;
    }
	
	public ResourceLink<FileHandle> createFileLink(String uriStr) {
		return createResourceLink(ThinURI.asNormalizedFileURI(null, uriStr));
	}

	public ResourceLink<HostHandle> createHostLink(String uriStr) {
		if (uriStr == null || uriStr.equals("")) 
			uriStr = ProtocolScheme.LOCAL.toURIStr("localhost");
		return createResourceLink(uriStr);
	}

	private <HANDLE_TYPE extends ResourceHandle> ResourceLink<HANDLE_TYPE> createResourceLink(String uriStr) {
		ThinURI uri = ThinURI.createBlind(uriStr);
		ProtocolScheme scheme = ProtocolScheme.valueOf(uri.getScheme().toUpperCase());
		Realm realm = resolveRealm(scheme, uri.getHost());
		return createResourceLink(uri, realm);
	}

	private <HANDLE_TYPE extends ResourceHandle> ResourceLink<HANDLE_TYPE> createResourceLink(ThinURI uri, Realm realm) {
	    if (realm == null)
			log.warn("No realm found for uri" + uri);
		
		return (ResourceLink<HANDLE_TYPE>) 
			((realm == null) ? 
					new ResourceLink<HANDLE_TYPE>(uri, null) : 
					realm.createResourceLink(uri));
    }
	
	public void test() {
		for (AccessControl ac : getAccessControls())
	        for (Realm realm : ac.getRealms()) realm.test();
    }
}

