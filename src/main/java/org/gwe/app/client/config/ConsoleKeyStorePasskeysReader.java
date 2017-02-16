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

package org.gwe.app.client.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.app.client.GWEConsoleReader;
import org.gwe.app.client.ProgressTracker;
import org.gwe.utils.Encryptor;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.AccessControl;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since Oct 13, 2008
 */
public class ConsoleKeyStorePasskeysReader {
	
	private static final String GWE_AUTH_BIN = "gwe-auth.bin";
	private static final String GWE_AUTH_XML = "gwe-auth.xml";
	
	private ProgressTracker tracker = ProgressTracker.CONSOLE_TRACKER;
	private String authBin;
	private String authXml;
	private String encryptionToken;
	private boolean offerToOverride;

	private GWEConsoleReader consoleReader;
	
	public ConsoleKeyStorePasskeysReader(InstallationFiles configFiles, String encryptionToken, boolean offerToOverride) throws IOException {
		this.authBin = configFiles.getConfigFilePath(GWE_AUTH_BIN);
		this.authXml = configFiles.getConfigFilePath(GWE_AUTH_XML);
	    this.encryptionToken = encryptionToken;
		this.consoleReader = new GWEConsoleReader(tracker);
		this.offerToOverride = offerToOverride;
    }
	
	public KeyStore readKeyStore() throws FileNotFoundException {
		Map<String, String> accountsPasskeys = new HashMap<String, String>();
		
		// FIXME: This will invalidate the feature of providing an alternative auth configuration file through the "-conf" argument
		KeyStore encKS = getEncryptedKeyStore();

		if (encKS == null) {
			tracker.trackProgress("No encrypted key store available! Generating a new one from scratch...");
		} else {
			accountsPasskeys = getPassKeys(encKS);
		}
		
		KeyStore keys = new KeysXMLConfigFile(authXml).getModel();
	    for (AccessControl ac : keys.getAccessControls()) 
	    	if (ac.getAccount().missingPasskey()) 
	    		loadPasskey(accountsPasskeys, ac);

	    writeEncryptedKeyStore(keys);
	    tracker.trackProgress("");
		return keys;
	}

	private Map<String, String> getPassKeys(KeyStore encKS) {
	    Map<String, String> accountsPasskeys = new HashMap<String, String>();
	    for (AccessControl ac : encKS.getAccessControls())
        	for (String id : ac.getAccountIds())
            	accountsPasskeys.put(id, ac.getAccount().getPasskey());
	    
	    return accountsPasskeys;
    }
	
	private void loadPasskey(Map<String, String> accountsPasskeys, AccessControl ac) {
	    AccountInfo account = ac.getAccount();
		List<String> ids = ac.getAccountIds();
		for (String id : ids) {
	    	String passKey = accountsPasskeys.get(id);
	    	if (passKey != null) {
	    		account.setPasskey(passKey);
	    		break;
	    	}
	    }

	    String id = ids.get(0);
	    if (!account.missingPasskey()) {
	        if (offerToOverride && consoleReader.readBoolean("Override pass key for [" + ids.get(0) + "]?"))
	        	readPasskey(account, id);
	    } else {
	    	readPasskey(account, id);
	    }
    }

	private void readPasskey(AccountInfo account, String id) {
	    try {
	        account.setPasskey(consoleReader.readPasskey("pass key for [" + id + "] : " , true));
	    } catch (IOException e) {
	    	tracker.trackProgress("Could not read the new pass key for [" + id + "]!");
	    }
    }

	private KeyStore getEncryptedKeyStore() {
	    long serKSTime = getFileTime(authBin);
		
		if (serKSTime != 0) {
			boolean outdated = serKSTime < getFileTime(authXml);
			if (outdated) {
				tracker.trackProgress("Outdated encrypted key store found!");
				if (consoleReader.readBoolean("Use it as a starting point to build an updated one?"))
					return readEncryptedKeyStore();
			} else
				return readEncryptedKeyStore();
		}

		return null;
	}

	private long getFileTime(String confFileName) {
	    return new File(confFileName).lastModified();
    }

	private KeyStore readEncryptedKeyStore() {
	    KeyStore result = tryToReadEncryptedKeyStore();
	    while (result == null) {
	    	tracker.trackProgress("Could not read encrypted key store!");
	    	if (consoleReader.readBoolean("Try with a different key token?")) {
		    	encryptionToken = null;
	    		result = tryToReadEncryptedKeyStore();
	    	} else {
	    		return null;
	    	}
	    }
	    return result;
    }

	private KeyStore tryToReadEncryptedKeyStore() {
		if (encryptionToken == null) {
			try {
				encryptionToken = consoleReader.readPasskey("Key store encryption token: ", false);
            } catch (IOException e) {
            	tracker.trackProgress("Could not read encryption token: " + e);
            	return null;
            }
		}
		
        try {
    		byte[] encryptedEncodedKS = IOUtils.readFile(authBin);
    		byte[] content = new Encryptor(encryptionToken).decrypt(encryptedEncodedKS);
			return IOUtils.deserializeObject(content);
        } catch (Exception e) {
        	tracker.trackProgress("Cannot read encrypted keystore: " + e);
        	return null;
        }
    }

	private void writeEncryptedKeyStore(KeyStore keys) {
        try {
        	byte[] serKS = IOUtils.serializeObject(keys);
    		byte[] content = new Encryptor(encryptionToken).encrypt(serKS);
    		FileOutputStream fos = new FileOutputStream(authBin, false);
			fos.write(content);
			fos.close();
        } catch (Exception e) {
        	tracker.trackProgress("Cannot write encrypted key store: " + e);
        }
    }

	public static void main(String[] args) throws Exception {
		new ConsoleKeyStorePasskeysReader(new InstallationFiles("/Users/admin/work/eclipse-ws/gwe-core/user"), "grid wizard enterprise", false).readKeyStore();
	}
}

