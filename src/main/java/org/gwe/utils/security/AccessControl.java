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


/**
 * @author Marco Ruiz
 * @since Oct 27, 2008
 */
public class AccessControl implements Serializable {
	
	public static AccessControl createDefaultAC(ProtocolScheme scheme, String host) {
		return new AccessControl(AccountInfo.createLocalAccount(), new Realm(scheme.toString(), host, "localhost"));
	}

	private static List<Realm> createRealmList(Realm... realms) {
	    ArrayList<Realm> result = new ArrayList<Realm>();
	    for (Realm realm : realms) result.add(realm);
	    return result;
    }

	private AccountInfo account;
	private List<Realm> realms;
	
    public AccessControl() { super(); }

	public AccessControl(AccountInfo account, Realm... realms) {
		this(account, createRealmList(realms));
    }

	public AccessControl(AccountInfo account, List<Realm> realms) {
		this.account = account;
	    this.realms = realms;
	    for (Realm realm : realms) realm.setAccount(account);
    }

	public AccountInfo getAccount() {
    	return account;
    }

	public void setAccount(AccountInfo account) {
    	this.account = account;
    }

	public List<Realm> getRealms() {
    	return realms;
    }
	
	public Realm findMatchingRealm(ProtocolScheme scheme, String host) {
	    for (Realm currRealm : getRealms()) {
	        boolean currRealmMatches = currRealm.implies(scheme.toString(), host);
	        if (currRealmMatches) return currRealm;
	    }
	    return null;
    }
	
	public List<String> getAccountIds() {
		List<String> result = new ArrayList<String>();
		String privateKeyFileName = account.getPrivateKeyFileName();
    	if (privateKeyFileName != null && !"".equals(privateKeyFileName)) {
        	result.add(account.getPrivateKeyFileName());
        } else {
        	for (Realm	realm : realms)
        		result.add((account.getUser() + "@" + realm.toString()));
        }
    	return result;
	}
}

