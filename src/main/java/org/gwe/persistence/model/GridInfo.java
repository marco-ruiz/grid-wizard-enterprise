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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Aug 7, 2007
 */
public class GridInfo {

	private static Log log = LogFactory.getLog(GridInfo.class);
	
    private List<HeadResourceInfo> headResources = new ArrayList<HeadResourceInfo>();

    public GridInfo() {}
    
    public GridInfo(HeadResourceInfo... headResources) {
    	for (HeadResourceInfo daemonInfo : headResources)
	        addHeadResource(daemonInfo);
    }
    
	public void addHeadResource(HeadResourceInfo daemonInfo) {
		headResources.add(daemonInfo);
    }

	public void setHeadResources(List<HeadResourceInfo> headResources) {
    	this.headResources = headResources;
    }

	public List<HeadResourceInfo> getHeadResources() {
		return headResources;
	}
	
	public HeadResourceInfo getHeadResource(String name) {
		if (name != null && !"".equals(name)) {
			for (HeadResourceInfo daemon : headResources)
				if (name.equals(daemon.getName())) return daemon;
		}
		return null;
	}
	
	public void validate() {
	    Set<HeadResourceInfo> misconfiguredDaemons = new HashSet<HeadResourceInfo>();
		for (HeadResourceInfo headRes : headResources) {
	        String host = headRes.getHost();
			if (host == null || "".equals(host)) {
	        	misconfiguredDaemons.add(headRes);
	        	log.warn("Ignoring configured cluster " + headRes.getName() + " because it is missing its host address value.");
	        }
	        if (headRes.getInstallRootPath() == null || "".equals(headRes.getInstallRootPath())) {
	        	if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
	        		headRes.setInstallRootPath(System.getProperty("user.home"));
		        	log.info("Localhost cluster is missing its installation root path value. Using user's home...");
	        	} else {
		        	misconfiguredDaemons.add(headRes);
		        	log.warn("Ignoring configured cluster " + headRes.getName() + " because is missing its installation root path value.");
	        	}
	        }
        }
		headResources.removeAll(misconfiguredDaemons);
    }
}
