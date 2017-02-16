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
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.ResourceHandle;

/**
 * A realm is a collection of resources that have the same authentication information.
 *
 * @author Marco Ruiz
 * @author Neil Jones
 * @since Jun 18, 2007
 */
public class Realm implements Serializable {

	private static Log log = LogFactory.getLog(Realm.class);

	private static List<String> semicolonSplitted(String target) {
		String[] entries = (target != null) ? target.split(";") : new String[]{};
		List<String> results = new ArrayList<String>(entries.length);
		for (String entry : entries) results.add(entry.toLowerCase());
		return results;
    }

	private String schemes;
	private String domain;
	private String testHost;
	private AccountInfo account;
	private RealmTestResult testResult;

	public Realm() { super(); }

    public Realm(String schemes, String domain, String testHost) {
		this.schemes = schemes;
		this.domain = domain;
		this.testHost = testHost;
    }
    
	public void setAccount(AccountInfo acct) {
		this.account = acct;
    }

    public AccountInfo getAccount() {
    	return account;
    }

	public String getDomain() {
    	return domain;
    }

	public String getTestHost() {
    	return testHost;
    }
	
	public RealmTestResult getTestResult() {
    	return testResult;
    }

    public boolean implies(String otherScheme, String otherDomain) {
    	if (!acceptsScheme(otherScheme)) return false;
    	if (otherDomain == null) otherDomain = "";
        String[] hostParts = otherDomain.split("\\.");
        for(int ii = 0; ii < hostParts.length; ++ii) {
            String hostStr = (ii == 0) ? "" : "*";
            for(int jj = ii; jj < hostParts.length; ++jj)
                hostStr = (hostStr.length() != 0) ? hostStr + "." + hostParts[jj] : hostParts[jj];
        
            if (domain.equals(hostStr)) return true;
        }
        return false;
    }

	public boolean acceptsScheme(ProtocolScheme scheme) {
	    return acceptsScheme(scheme.toString());
    }
    
	public boolean acceptsScheme(String scheme) {
	    return semicolonSplitted(schemes).contains(scheme.toLowerCase());
    }
    
	public <HANDLE_TYPE extends ResourceHandle> ResourceLink<HANDLE_TYPE> createResourceLink(ThinURI uri) {
		return new ResourceLink<HANDLE_TYPE>(uri, account);
    }
	
	public void test() {
		testResult = determineTestResult();
		log.info("Test result of realm ["  + this + "]: " + testResult.getMessage());
	}
	
	private RealmTestResult determineTestResult() {
	    // Missing authentication
		if (getAccount() == null) 
	    	return RealmTestResult.MISSING_ACCOUNT; 

		if (getAccount().missingPasskey()) 
	    	return RealmTestResult.MISSING_PASSKEY; 

	    if (getTestHost() == null || "".equals(getTestHost())) 
	    	return RealmTestResult.MISSING_TEST_HOST;  
    	
		// No host to test against
		if (!acceptsScheme(ProtocolScheme.SSH))
	    	return RealmTestResult.NO_SSH_REALM;  
			
        try {
        	createTestHostLink().createHandle().close();
        } catch (HandleCreationException e) {
        	// Authentication failed with test host - could work with other hosts though
    	    return RealmTestResult.ERROR;
        } catch (HandleOperationException e) {
        	// Closing handle failed with test host - ignore, connecting is the only thing we are testing
        }
		// Authentication worked at least with test host
    	return RealmTestResult.OK;
    }

	public ResourceLink<ResourceHandle> createTestHostLink() {
	    String uriStr = ProtocolScheme.SSH.toURIStr(getTestHost());
	    ThinURI uri = ThinURI.createBlind(uriStr);
	    return createResourceLink(uri);
    }
	
    public String toString() {
		boolean started = false;
    	String result = "";
    	for (String ele : semicolonSplitted(schemes)) {
			if (started) result += ";";
    		result += ele;
    		started = true;
    	}
    	return result + ThinURI.SCHEME_SEPARATOR + domain;
    }
}

