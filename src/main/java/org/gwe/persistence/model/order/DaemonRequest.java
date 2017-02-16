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

import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since Jul 12, 2007
 */
public abstract class DaemonRequest<PARAMS_TYPE extends Serializable> implements Serializable {

	protected String execId;
	protected KeyStore keys;
	protected PARAMS_TYPE parameters;
	protected long maxJobRunningTime;
	
	public void setExecId(String jobId) { 
		this.execId = jobId; 
	}
	
	public String getExecId() {
    	return execId;
    }

	public void setKeys(KeyStore keys) { 
		this.keys = keys; 
	}
	
	public void setParameters(PARAMS_TYPE parameters) { 
		this.parameters = parameters; 
	}
	
	public void setMaxJobRunningTime(long maxJobRunningTime) { 
		this.maxJobRunningTime = maxJobRunningTime; 
	}
	
	public abstract Serializable process(String workspace) throws Exception;
}
