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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Jan 26, 2007
 */
public class AccountInfo implements Serializable {
	
	private static Log log = LogFactory.getLog(AccountInfo.class);
	
	private static final String MISSING_VALUE = "?";
	private static byte[] NULL_BYTE_ARRAY = new byte[]{};
	
	public static final AccountInfo NO_AUTH_ACCOUNT = new AccountInfo("", "");
	
	public static AccountInfo createLocalAccount() {
		return new AccountInfo(System.getProperty("user.name"), null);
	}
	
	// Account identification info
	private String user;
	private Properties props = new Properties();
	
	// Account authentication info - password based
	private String password = null;
	
	// Account authentication info - private key based
	private String passphrase = null;
	
	private String privateKeyFileName;
	private String publicKeyFileName;
	
	private byte[] privateKey = null;
	private byte[] publicKey = null;
	
	private boolean keyFilesError = false;
	
	// Account authentication info - gss based
//	private GSSCredential credential;
	
	public AccountInfo(String user, String passphrase, String privateKeyFilename, String publicKeyFilename) throws FileNotFoundException, IOException {
		this(user, passphrase, IOUtils.readFile(privateKeyFilename), IOUtils.readFile(publicKeyFilename));
		if (privateKeyFilename != null &&  !"".equals(privateKeyFilename) && publicKey == null)
			this.publicKey = IOUtils.readFile(privateKeyFilename + ".pub");
	}

	/**
	 * Private key based authenticated account
	 * 
	 * @param accountName
	 * @param user
	 * @param passKey
	 * @param privateKey
	 */
	public AccountInfo(String user, String passKey, byte[] privateKey, byte[] publicKey) {
		this(user);
		if (privateKey != null && privateKey != NULL_BYTE_ARRAY) { 
			this.passphrase = passKey;
			this.privateKey = privateKey;
			this.publicKey = publicKey;
		} else {
			this.password = passKey;
		}
	}
	
	/**
	 * Password based authenticated account
	 * 
	 */
	public AccountInfo(String user, String password) {
		this(user, password, NULL_BYTE_ARRAY, NULL_BYTE_ARRAY);
//		this.password = (password == null) ? "" : password;
	}

	private AccountInfo(String userName) {
		this.user = userName;
	}
	
	public void init() {
		if (privateKeyFileName != null &&  !"".equals(privateKeyFileName) && (publicKey == null || "".equals(publicKey)))
			this.publicKeyFileName = privateKeyFileName + ".pub";
		
		if (privateKey == null || privateKey == NULL_BYTE_ARRAY) { 
            this.privateKey = readKeyFileWithNoExceptions(privateKeyFileName);
			this.publicKey = readKeyFileWithNoExceptions(publicKeyFileName);
		}
    }
	
	public boolean isKeyFilesError() {
    	return keyFilesError;
    }

	private byte[] readKeyFileWithNoExceptions(String fileName) {
		try {
	        return IOUtils.readFile(fileName);
        } catch (Exception e) {
        	keyFilesError = true;
        	log.warn("Key file " + fileName + "could not be loaded.", e);
        	this.passphrase = "";
        	return NULL_BYTE_ARRAY;
        }
	}

	public String getUser() {
		return user;
	}
	
	public String getProperty(String propName) {
		return props.getProperty(propName);
	}
	
	public void setProperties(Properties properties) {
		this.props = properties;
	}
	
	public String getPassphrase() {
		return passphrase;
	}
	
	public String getPrivateKeyFileName() {
    	return privateKeyFileName;
    }

	public byte[] getPrivateKey() {
		return (privateKey == null) ? privateKey : privateKey.clone();
	}
	
	public byte[] getPublicKey() {
		return (publicKey == null) ? publicKey : publicKey.clone();
	}
	
	public String getPassword() {
		return password;
	}
/*
	public GSSCredential getCredential() {
		return credential;
	}
	
	public void setCredential(GSSCredential credential) {
		this.credential = credential;
	}
*/
/*
	public void setCredential(String proxyStr) throws GSSException {
		GlobusGSSManagerImpl mgr = new GlobusGSSManagerImpl();
		credential = mgr.createCredential(proxyStr.getBytes(), ExtendedGSSCredential.IMPEXP_OPAQUE,
		        GSSCredential.DEFAULT_LIFETIME, null, GSSCredential.ACCEPT_ONLY);
	}
*/

	public AccountInfo clone() {
		return (this.password != null) ? 
			new AccountInfo(user, password) : 
			new AccountInfo(user, passphrase, privateKey, publicKey);
	}
	
	public String getPasskey() {
		return (this.password != null) ? password : passphrase;
	}

	public void setPassword(String password) {
    	this.password = password;
    }

	public void setPassphrase(String passphrase) {
    	this.passphrase = passphrase;
    }
	
	public void setPasskey(String passkey) {
		if (missingPassword())   setPassword(passkey);
		if (missingPassphrase()) setPassphrase(passkey);
	}

	public boolean missingPasskey() {
		return missingPassword() || missingPassphrase();
	}
	
	public boolean missingPassword() {
	    return MISSING_VALUE.equals(password);
    }

	public boolean missingPassphrase() {
	    return MISSING_VALUE.equals(passphrase);
    }
    	
	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((passphrase == null) ? 0 : passphrase.hashCode());
	    result = prime * result + ((password == null) ? 0 : password.hashCode());
	    result = prime * result + Arrays.hashCode(privateKey);
	    result = prime * result + Arrays.hashCode(publicKey);
	    result = prime * result + ((user == null) ? 0 : user.hashCode());
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
	    AccountInfo other = (AccountInfo) obj;
	    if (passphrase == null) {
		    if (other.passphrase != null)
			    return false;
	    } else if (!passphrase.equals(other.passphrase))
		    return false;
	    if (password == null) {
		    if (other.password != null)
			    return false;
	    } else if (!password.equals(other.password))
		    return false;
	    if (!Arrays.equals(privateKey, other.privateKey))
		    return false;
	    if (!Arrays.equals(publicKey, other.publicKey))
		    return false;
	    if (user == null) {
		    if (other.user != null)
			    return false;
	    } else if (!user.equals(other.user))
		    return false;
	    return true;
    }
}
