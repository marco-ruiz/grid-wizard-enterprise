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

import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.JobInfoIdGenerator;
import org.gwe.persistence.model.OrderExecutionProfileInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.IOUtils;
import org.gwe.utils.concurrent.BlockingList;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Marco Ruiz
 * @since Mar 10, 2008
 */
public class UserDomain extends BaseDomain {

	private static Log log = LogFactory.getLog(UserDomain.class);
	
	private static final Pattern EMAIL_PATTERN = Pattern.compile("(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*");

	private BlockingList<OrderInfo> newOrders = new BlockingList<OrderInfo>();

	private boolean isEmail(String target) {
		Matcher matcher = EMAIL_PATTERN.matcher(target);
		if (!matcher.find()) return false;
		if (matcher.find()) return false;
		return true;
	}
	
	//=====================
	// Server API Services
	//=====================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void setupDaemon(HeadResourceInfo daemonInfo) {
		headResourceDAO.save(daemonInfo);
	}

	public void shutdownDaemon(DaemonConfigDesc admin) {
		System.exit(0);
	}
	
	// ==============
	//  USER PROFILE
	// ==============
	public void updateConfig(DaemonConfigDesc config) throws Exception {
		DaemonConfigDesc persistedConfig = getConfig();
		persistedConfig.setKeys(config.getKeys());
		
		// Update in DB
		updateHeadResource(config.getHeadResource());
		
		// Save serialized user info
		HeadResourceInfo daemonInfo = persistedConfig.getHeadResource();
        IOUtils.serializeObject(config, new FileOutputStream(daemonInfo.getInstallation().getConfigurationFilePath()));
    }

	public void updateHeadResource(HeadResourceInfo daemonInfo) {
		HeadResourceInfo persistedDaemon = headResourceDAO.get(daemonInfo.getId());
		persistedDaemon.setName(           daemonInfo.getName());
		persistedDaemon.setMaxHijackMins(  daemonInfo.getMaxHijackMins());
		persistedDaemon.setMaxIdleMins(    daemonInfo.getMaxIdleMins());
		persistedDaemon.setQueueSize(      daemonInfo.getQueueSize());
		persistedDaemon.setHeartBeatPeriodSecs(daemonInfo.getHeartBeatPeriodSecs());
		headResourceDAO.updateAndEvict(persistedDaemon);
    }

	public void updateDefaultExecutionProfile(OrderExecutionProfileInfo executionProfile) {
		OrderExecutionProfileInfo persistedExecutionProfile = getConfig().getDefaultExecutionProfile();
    }

	// ====================
	//  ORDERS & JOBS INFO
	// ====================
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public List<OrderInfo> getOrdersDefined() {
		List<OrderInfo> orders = orderDAO.getList();
		orders.size(); // load orders
		return orders;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public List<OrderInfo> getOrdersList(boolean includeJobs) {
		List<OrderInfo> result = getOrdersDefined();
		if (includeJobs)
			for (OrderInfo order : result) order.getJobs().size();
		return result;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public List<OrderInfo> getOrdersByDescription(String description) {
		return orderDAO.getByDescription(description);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public JobInfo getJobDetails(int orderId, int jobNum) {
		OrderInfo order = getOrder(orderId, false);
		String jobId = JobInfoIdGenerator.generateId(orderId, order.getTotalJobsCount(), jobNum);
		return jobDAO.get(jobId);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public List<JobExecutionInfo> getExecutionDetails(int orderId, int jobNum) {
		OrderInfo order = getOrder(orderId, false);
		String jobId = JobInfoIdGenerator.generateId(orderId, order.getTotalJobsCount(), jobNum);
		return jobExecutionDAO.getBelongingToJob(jobId);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.READ_UNCOMMITTED, readOnly=true)
	public OrderInfo getOrder(int orderId, boolean includeJobs) {
		return orderDAO.getOrder(orderId, includeJobs);
	}

	// ================
	//  ORDERS CONTROL
	// ================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public OrderInfo persistOrder(OrderInfo order) {
		// Preparation and error checking
		if (order == null) return null;

		orderDAO.save(order);
		return order;
	}
	
	public void queueOrder(OrderInfo order) {
		order.generateJobs(getConfig(), getConfig().getDefaultExecutionProfile().clone());
		newOrders.add(order);
	}

	public List<String> previewOrder(OrderInfo order) throws Exception {
		return order.getDescriptor().generateCommands(getConfig());
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void pauseOrder(int orderId, boolean pause) {
		orderDAO.setPause(orderId, pause);
		if (!pause) newOrders.forceWakeUp();
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void abortOrder(int orderId) {
		getOrder(orderId, false).setAborted(true);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void deleteOrder(int orderId) {
		orderDAO.markAsDeleted(orderId);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void swapPriorities(int orderId1, int orderId2) {
		orderDAO.swapPriorities(orderId1, orderId2);
    }

	public void publishOrder(int orderId, String host, String path) {
//		this.getConfig().getDaemonHostLink()
		
//		orderDAO.swapPriorities(orderId1, orderId2);
    }

	public void cleanupDisposedAllocations() {
		getConfig().getHeadResource().getInstallation().cleanupDisposedAllocFolder();
    }
	
	//==============================================
	// METHODS TO BE INVOKED BY BACKGROUND SERVICES
	//==============================================
	@Transactional(propagation=Propagation.NEVER)
	public List<OrderInfo> dequeueUnprocessedOrders() {
		return newOrders.takeAll();
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<OrderInfo> generateJobs(List<OrderInfo> newOrdersCopy) {
		if (newOrdersCopy != null) {
		    for (OrderInfo order : newOrdersCopy) 
				jobDAO.saveAll(order.getJobs());
			
		    orderDAO.saveOrUpdateAll(newOrdersCopy);
		}
		return newOrdersCopy;
    }
}

