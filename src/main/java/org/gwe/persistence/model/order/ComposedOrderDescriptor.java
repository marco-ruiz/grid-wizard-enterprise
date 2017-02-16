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

package org.gwe.persistence.model.order;

import java.util.ArrayList;
import java.util.List;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.live.OrderLive;

/**
 * @author Marco Ruiz
 * @since Sep 28, 2007
 */
public class ComposedOrderDescriptor<DRP_TYPE> extends OrderDescriptor {
	
	private List<OrderDescriptor> descriptors;

	public final void addDescriptor(OrderDescriptor desc) {
		descriptors.add(desc);
	}
	
    public List generateJobDescriptors(DaemonConfigDesc config) throws Exception {
		List<JobInfo> results =  new ArrayList<JobInfo>();
		for (OrderDescriptor desc : descriptors) 
			results.addAll(desc.generateJobDescriptors(config));
		return results;
    }

    public List<String> generateCommands(DaemonConfigDesc config) throws Exception {
    	List<String> results =  new ArrayList<String>();
		for (OrderDescriptor desc : descriptors) 
			results.addAll(desc.generateCommands(config));
		return results;
    }

    public String toString() {
		String result = "";
		for (OrderDescriptor desc : descriptors) result += desc.toString() + "\n";
		return result;
	}

	public void initExecution(OrderLive orderRC) {
		// TODO Auto-generated method stub
	}

	public void finalizeExecution(OrderLive orderRC) {
		// TODO Auto-generated method stub
	}

    public List<String> getVarNames() {
	    return new ArrayList<String>();
    }
}

