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

package org.gwe.app.daemon.domain;

import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.persistence.dao.AllocationInfoDAO;
import org.gwe.persistence.dao.ComputeResourceInfoDAO;
import org.gwe.persistence.dao.HeadResourceInfoDAO;
import org.gwe.persistence.dao.JobExecutionInfoDAO;
import org.gwe.persistence.dao.JobInfoDAO;
import org.gwe.persistence.dao.OrderInfoDAO;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.ResourceLink;


/**
 * 
 * @author Marco Ruiz
 * @since Oct 2, 2007
 */
public class BaseDomain {

	private DaemonConfigDesc config;
	private ResourceLink<HostHandle> authLink = null;
	
	protected HeadResourceInfoDAO headResourceDAO;
	protected OrderInfoDAO orderDAO;
	protected AllocationInfoDAO allocationDAO;
	protected ComputeResourceInfoDAO computeResourceDAO;
	protected JobInfoDAO jobDAO;
	protected JobExecutionInfoDAO jobExecutionDAO;

	public DaemonConfigDesc getConfig() {
    	return config;
    }
	
	public void setConfig(DaemonConfigDesc config) {
    	this.config = config;
    }

	public void verifyAccount(AccountInfo applyingAuth) throws PasswordMismatchException {
		if (authLink == null) authLink = config.getDaemonHostLink();
		if (!applyingAuth.equals(authLink.getAccountInfo())) 
			throw new PasswordMismatchException(authLink);
	}
	
	public HeadResourceInfoDAO getHeadResourceDAO() {
		return headResourceDAO;
	}
	
	public void setHeadResourceDAO(HeadResourceInfoDAO headResourceDAO) {
		this.headResourceDAO = headResourceDAO;
	}
	
	public OrderInfoDAO getOrderDAO() {
		return orderDAO;
	}
	
	public void setOrderDAO(OrderInfoDAO orderDAO) {
		this.orderDAO = orderDAO;
	}
	
	public AllocationInfoDAO getAllocationDAO() {
		return allocationDAO;
	}
	
	public void setAllocationDAO(AllocationInfoDAO allocationDAO) {
		this.allocationDAO = allocationDAO;
	}
	
	public ComputeResourceInfoDAO getComputeResourceDAO() {
		return computeResourceDAO;
	}
	
	public void setComputeResourceDAO(ComputeResourceInfoDAO computeResourceDAO) {
		this.computeResourceDAO = computeResourceDAO;
	}

	public JobInfoDAO getJobDAO() {
		return jobDAO;
	}
	
	public void setJobDAO(JobInfoDAO jobDAO) {
		this.jobDAO = jobDAO;
	}
	
	public JobExecutionInfoDAO getJobExecutionDAO() {
    	return jobExecutionDAO;
    }

	public void setJobExecutionDAO(JobExecutionInfoDAO jobExecutionDAO) {
    	this.jobExecutionDAO = jobExecutionDAO;
    }
}
