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

package org.gwe.persistence.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ServerAPILink;
import org.gwe.app.Distribution;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.tunneling.TunneledSocketFactory;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.ResourceLink;

/**
 * @author Marco Ruiz
 * @since Aug 7, 2007
 */
@Entity
public class DaemonConfigDesc extends BaseModelInfo<Integer> {

	private static Log log = LogFactory.getLog(DaemonConfigDesc.class);

	@Id
	private Integer id;
	
	private Distribution distribution;
	
	private String email;
	private KeyStore keys;
	
	// There are many head resource records in the DB. This one is the one that corresponding to the running daemon
	// Each daemon will have a different head resource associated with this same, unique, user.
	@OneToOne(fetch = FetchType.EAGER)
    private HeadResourceInfo headResource;

	@OneToOne(fetch = FetchType.EAGER)
	private OrderExecutionProfileInfo defaultExecutionProfile = new OrderExecutionProfileInfo();

	public DaemonConfigDesc() {}
	
	public DaemonConfigDesc(HeadResourceInfo headResource, KeyStore keys, Distribution distribution) {
		setHeadResource(headResource);
		setKeys(keys);
		setDistribution(distribution);
	}
	
	public Integer getId() { 
		return id; 
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Distribution getDistribution() {
    	return distribution;
    }

	public void setDistribution(Distribution distribution) {
    	this.distribution = distribution;
    }

	public OrderExecutionProfileInfo getDefaultExecutionProfile() {
    	return defaultExecutionProfile;
    }

	public void setDefaultExecutionProfile(OrderExecutionProfileInfo defaultExecutionProfile) {
    	this.defaultExecutionProfile = defaultExecutionProfile;
    }
	
	public HeadResourceInfo getHeadResource() {
    	return headResource;
    }

	public void setHeadResource(HeadResourceInfo headResource) {
    	this.headResource = headResource;
    }
	
	public KeyStore getKeys() {
	    return keys;
    }

	public void setKeys(KeyStore keys) {
	    this.keys = keys;
		initializeServices();
    }
	
	public String getConnectionURL() {
		return headResource.getConnectionURL(keys);
	}
	
	public ResourceLink<HostHandle> getDaemonHostLink() {
        return getKeys().createHostLink(headResource.getCompURI());
	}
	
	public ServerAPILink createAPILink() {
		return new ServerAPILink(headResource, getDaemonHostLink());
	}
	
	public String getAllocWorkspace(int allocId) {
		return headResource.getInstallation().getAllocsWorkspacePath(allocId);
	}

	// COMPUTED PROPERTIES 
	public void initializeServices() {
		TunneledSocketFactory.setKeys(keys);
	}
}

