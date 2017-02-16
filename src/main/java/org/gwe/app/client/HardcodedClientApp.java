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

package org.gwe.app.client;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.ClientOrderBasedQuery;
import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.api.ValidationException;
import org.gwe.api.exceptions.GWEDomainException;
import org.gwe.app.client.admin.ClientDaemonAppManager;
import org.gwe.app.client.admin.InstallerException;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.gwe.utils.security.CredentialNotFoundException;

/**
 * @author Marco Ruiz
 * @since Jan 25, 2008
 */
public class HardcodedClientApp {

	private static Log log = LogFactory.getLog(HardcodedClientApp.class);

	public static void main(String[] args) throws CredentialNotFoundException, RemoteException, ServerAPIConnectionException, ValidationException, GWEDomainException, InstallerException {
		ClientConfig appConfig = new ClientConfig("birn.cluster-0.nbirn.net", "/export2/mruiz", "mruiz", "password");
		HeadResourceInfo daemonInfo = appConfig.getGrid().getHeadResources().get(0);
		appConfig.setTracker(ProgressTracker.CONSOLE_TRACKER);
		ClientDaemonAppManager daemonAppManager = new ClientDaemonAppManager(appConfig.createDaemonConfig(daemonInfo));
		try {
	        daemonAppManager.installDaemonIfMissing();
        } catch (Exception e) {}
//        daemonAppManager.launchDaemon();

        Session4ClientAPIEnhancer session = appConfig.getSessionsRepository().getSession(daemonInfo, true);
		String p2elStmt = "";
		OrderInfo order = new OrderInfo(new POrderDescriptor<Serializable>(p2elStmt));
		session.queueOrder(order);
		session.getOrdersList(true);
		
		String result = session.doOrderBasedQuery(ClientOrderBasedQuery.LIST_ORDERS, -1, -1);
		System.out.println(result);
	}
	
/*
	public void executeOrder(OrderInfo order) throws InterruptedException {
		order.generateJobs(new HeadResourceInfo("localhost", 1099));

		List<Callable<Object>> jobRunners = new ArrayList<Callable<Object>>();
		for (JobInfo job : order.getJobs()) {
			processJob(job);
        }
		
		ThreadPoolUtils.createThreadPool("Local Job Runner").invokeAll(jobRunners);
	}

	private void processJob(JobInfo job) throws Exception {
	    job.preProcess(new JobLive(job, job.getOrder()));
	    Serializable result = job.getRequest().process("[WORKSPACE]");
	    job.postProcess(null, result);
    }
*/
}

