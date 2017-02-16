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

package org.gwe.persistence.model.live;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.OrderExecutionProfileInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Mar 18, 2008
 */
public class OrderLive {

	private OrderInfo info;
    private DaemonConfigDesc config;
	
    public OrderLive(DaemonConfigDesc config, OrderInfo info) {
    	this.info = info;
    	this.config = config;
		info.getDescriptor().initExecution(this);
    }

	public OrderInfo getInfo() { 
		return info; 
	}
	
	public DaemonConfigDesc getConfig() { 
		return config; 
	}
	
	public String getUserHomePath() {
    	return config.getHeadResource().getInstallRootPath();
    }

    public void unload() {
    	info.getDescriptor().finalizeExecution(this);
    }
    
	public boolean canCleanUp() {
    	OrderExecutionProfileInfo execProfile = info.getExecutionProfile();
		return execProfile.isCleanUpModeAlways() || (execProfile.isCleanUpModeOnSuccess());
	}
}
