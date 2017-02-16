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

package org.gwe.drivers.resManagers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.HeadResourceInfo;

/**
 * @author Marco Ruiz
 * @since Aug 9, 2007
 */
public class GridResourceManager {

	private static Log log = LogFactory.getLog(GridResourceManager.class);

	private ResourceManagerDriver driver4DiscoveredLocalRM;
	protected List<ResourceManagerDriver> drivers;
	private HeadResourceInfo daemonInfo;
	
	public GridResourceManager(List<ResourceManagerDriver> drivers, HeadResourceInfo daemonInfo) {
		this.drivers = drivers;
		this.daemonInfo = daemonInfo;
	}

	public synchronized ResourceManagerDriver getResourceManagerDriver() throws NoResourceManagerDiscoveredException {
		if (driver4DiscoveredLocalRM != null) return driver4DiscoveredLocalRM;
		
		for (ResourceManagerDriver currDriver : getOrderedResourceManagers()) {
			log.info("Testing job manager driver '" + currDriver.getClass().getName() + "'");
			if (currDriver.isSupportedJobManagerAvailable()) {
				log.info("Job manager driver '" + currDriver.getClass().getName() + "' detected a manageable Job Manager!");
				driver4DiscoveredLocalRM = currDriver;
				return driver4DiscoveredLocalRM;
			}
			log.info("Job manager driver '" + currDriver.getClass().getName() + "' did not detect a manageable Job Manager...");
		}
		throw new NoResourceManagerDiscoveredException();
	}

	private List<ResourceManagerDriver> getOrderedResourceManagers() {
		String resMgr = daemonInfo.getResourceManager();
		if (resMgr == null) return drivers;
		
	    List<ResourceManagerDriver> result = new ArrayList<ResourceManagerDriver>();
	    List<ResourceManagerDriver> remainingDrivers = new ArrayList<ResourceManagerDriver>(drivers);
		String[] resMgrs = resMgr.split(";");
		for (String resMgrId : resMgrs) {
            ResourceManagerDriver driver = extractDriver(resMgrId.toLowerCase().trim());
            if (driver != null) { 
            	result.add(driver);
            	remainingDrivers.remove(driver);
            }
        }
		result.addAll(remainingDrivers);
		return result;
    }

	private ResourceManagerDriver extractDriver(String resMgrId) {
		if (resMgrId != null && !resMgrId.equals("")) {
			for (ResourceManagerDriver currDriver : drivers) 
		        if (resMgrId.equals(currDriver.getId())) 
		        	return currDriver;
		}
	    return null;
    }
	
	public static void main(String[] args) {
		HeadResourceInfo daemonInfo = new HeadResourceInfo();
		List<ResourceManagerDriver> drivers = new ArrayList<ResourceManagerDriver>();
		drivers.add(setDriverId(new CondorDriver(), "condor"));
		drivers.add(setDriverId(new QsubDriver(), "qsub"));
		drivers.add(setDriverId(new MultiProcessesDriver(), "local"));
		
		printOrderedList(daemonInfo, drivers, null);
		printOrderedList(daemonInfo, drivers, "");
		printOrderedList(daemonInfo, drivers, "condor           ");
		printOrderedList(daemonInfo, drivers, "condor;qsub      ");
		printOrderedList(daemonInfo, drivers, "condor;qsub;local");

		printOrderedList(daemonInfo, drivers, "qsub;            ");
		printOrderedList(daemonInfo, drivers, "qsub;condor      ");
		printOrderedList(daemonInfo, drivers, "qsub;condor;local");
		
		printOrderedList(daemonInfo, drivers, "local            ");
		printOrderedList(daemonInfo, drivers, "local;condor     ");
		printOrderedList(daemonInfo, drivers, "local;condor;qsub");
		printOrderedList(daemonInfo, drivers, "local;condor;local");

		printOrderedList(daemonInfo, drivers, "condor;local");
	}

	private static ResourceManagerDriver setDriverId(ResourceManagerDriver driver, String id) {
	    driver.setId(id);
	    return driver;
    }

	private static void printOrderedList(HeadResourceInfo daemonInfo, List<ResourceManagerDriver> drivers, String resList) {
	    daemonInfo.setResourceManager(resList);
		List<ResourceManagerDriver> result = new GridResourceManager(drivers, daemonInfo).getOrderedResourceManagers();
		System.out.println(resList + "\t" + result);
    }
}

