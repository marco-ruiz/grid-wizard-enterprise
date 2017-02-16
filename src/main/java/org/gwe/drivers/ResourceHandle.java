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

package org.gwe.drivers;

import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

/**
 * @author Marco Ruiz
 * @since Dec 6, 2008
 */
public class ResourceHandle {
	
    protected ResourceLink link;

    public ResourceHandle(ResourceLink link) { 
    	this.link = link; 
    }
    
	public ResourceLink getLink() {
		return link;
	}
	
	protected HostHandle createHostHandle() throws HandleCreationException, REXException {
	    String host = link.getURI().getHost();
		ProtocolScheme scheme = (host == null || host.equals("")) ? ProtocolScheme.LOCAL : ProtocolScheme.SSH;
		ThinURI uri = ThinURI.create(scheme.toURIStr(host));
	    return (HostHandle) new ResourceLink<HostHandle>(uri, link.getAccountInfo()).createHandle();
    }
	
    public void close() throws HandleOperationException {}
}
