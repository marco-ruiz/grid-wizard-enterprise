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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.ResourceHandle;
import org.gwe.drivers.ResourceHandleFactory;
import org.gwe.drivers.netAccess.HostHandle;

/**
 * @author Marco Ruiz
 * @since Jul 2, 2007
 */
public class ResourceLink<HANDLE_TYPE extends ResourceHandle> implements Serializable {
	
	private static Log log = LogFactory.getLog(ResourceLink.class);

	private static ResourceHandleFactory repo;
	
	public static void setHandleFactory(ResourceHandleFactory repo) {
		ResourceLink.repo = repo;
    }

	protected ThinURI uri;
	protected AccountInfo account;
	
	public ResourceLink(ThinURI uri, AccountInfo account) {
		this.uri = uri;
		this.account = account;
		if (account == null)
			log.warn("Account not found for uri" + uri);
	}

	public AccountInfo getAccountInfo() {
		return account;
	}

	public ThinURI getURI() {
		return uri;
	}
	
	public String getURIHost() {
	    return (uri == null) ? null : uri.getHost();
    }

	public ResourceLink<HostHandle> cloneToHostLink(ProtocolScheme hostScheme) {
		ThinURI hostURI = new ThinURI(hostScheme, uri.getHost(), "");
		return new ResourceLink<HostHandle>(hostURI, account); 
	}
	
	public HANDLE_TYPE createHandle() throws HandleCreationException {
        return repo.createHandle(this);
	}
	
	public Class<HANDLE_TYPE> getHandleClass() {
        return repo.getHandleClass(this);
	}
	
	public String toString() {
		String accountName = (account != null) ? account.getUser() : "NONE";
		return "[" + accountName + "]@[" + uri + "]";
	}
}

