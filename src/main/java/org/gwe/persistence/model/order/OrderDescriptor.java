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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.live.OrderLive;

/**
 * @author Marco Ruiz
 * @since Aug 23, 2007
 */
public abstract class OrderDescriptor<DAEMON_REQUEST_PARAM_TYPE extends Serializable> implements Serializable {

	protected DAEMON_REQUEST_PARAM_TYPE parameters = null;

	protected Class<? extends DaemonRequest> daemonRequestClass = 
		(Class<? extends DaemonRequest>) OSCommandDaemonRequest.class;

	public void setDaemonRequestClassName(String clazzName) throws ClassNotFoundException {
		setDaemonRequestClass((Class<? extends DaemonRequest<DAEMON_REQUEST_PARAM_TYPE>>) Class.forName(clazzName));
	}

	public void setDaemonRequestClass(Class<? extends DaemonRequest<DAEMON_REQUEST_PARAM_TYPE>> daemonRequestClass) {
		this.daemonRequestClass = daemonRequestClass;
	}

	public Class<? extends DaemonRequest> getDaemonRequestClass() {
		return daemonRequestClass;
	}

	public void setParameters(DAEMON_REQUEST_PARAM_TYPE parameters) {
		this.parameters = parameters;
	}

	public DAEMON_REQUEST_PARAM_TYPE getParameters() {
		return parameters;
	}

	public final List<JobInfo> generateJobs(DaemonConfigDesc config) throws Exception {
		List<JobInfo> results =  new ArrayList<JobInfo>();
		
		int count = 1;
		for (JobDescriptor descriptor : generateJobDescriptors(config)) {
			if (descriptor != null) {
				try {
					DaemonRequest<DAEMON_REQUEST_PARAM_TYPE> daemonRequest = daemonRequestClass.newInstance();
					daemonRequest.setParameters(parameters);
					JobInfo job = new JobInfo(count++, descriptor, daemonRequest);
					results.add(job);
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		return results;
	}
	
	public abstract List<String> getVarNames();

	public abstract List<JobDescriptor> generateJobDescriptors(DaemonConfigDesc config) throws Exception;
	
    public abstract List<String> generateCommands(DaemonConfigDesc config) throws Exception;

	public abstract void initExecution(OrderLive orderRC);
	
	public abstract void finalizeExecution(OrderLive orderRC);
}

