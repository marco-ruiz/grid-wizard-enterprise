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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Jun 24, 2008
 */
enum Launch{ AUTO, MANUAL, NOTIFICATION; } 
enum CleanUp{ ALWAYS, ON_SUCCESS, NEVER; } 

@Entity
public final class OrderExecutionProfileInfo extends BaseModelInfo<Integer> {

	private static Log log = LogFactory.getLog(OrderExecutionProfileInfo.class);
	
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO) 
	private int id;
	
	private int launchMode = Launch.AUTO.ordinal();
	private int cleanUpMode = CleanUp.ALWAYS.ordinal();
	
	private int maxJobRunningTime = -1;
	private int maxRetries = 3;
	private int maxConcurrentRunningJobs = -1;
	private int maxPreparedJobs = 10;
	private int diskSpaceForVFS = -1;

	private boolean useVFSCache = true;
	private int mpiMachinesPerJob = 1;

//	@Lob
//    private Set<String> preInstallBundleIds = new HashSet<String>();
	
    public Integer getId() {
	    return id;
    }

	public int getLaunchMode() {
    	return launchMode;
    }

	public Launch getLaunchModeEnum() {
    	return Launch.values()[launchMode];
    }

	public void setLaunchMode(int launchMode) {
    	this.launchMode = launchMode;
    }

	public int getCleanUpMode() {
    	return cleanUpMode;
    }

	public CleanUp getCleanUpModeEnum() {
    	return CleanUp.values()[cleanUpMode];
    }

	public void setCleanUpMode(int cleanUpMode) {
    	this.cleanUpMode = cleanUpMode;
    }

	public boolean isCleanUpModeAlways() {
    	return cleanUpMode == CleanUp.ALWAYS.ordinal();
    }

	public boolean isCleanUpModeOnSuccess() {
    	return cleanUpMode == CleanUp.ON_SUCCESS.ordinal();
    }

	public boolean isCleanUpModeNever() {
    	return cleanUpMode == CleanUp.NEVER.ordinal();
    }

	public int getMaxJobRunningTime() {
    	return maxJobRunningTime;
    }

	public void setMaxJobRunningTime(int maxJobRunningTime) {
    	this.maxJobRunningTime = maxJobRunningTime;
    }

	public int getMaxRetries() {
    	return maxRetries;
    }

	public void setMaxRetries(int maxRetries) {
    	this.maxRetries = maxRetries;
    }

	public int getMaxConcurrentRunningJobs() {
    	return maxConcurrentRunningJobs;
    }

	public void setMaxConcurrentRunningJobs(int maxConcurrentRunningJobs) {
    	this.maxConcurrentRunningJobs = maxConcurrentRunningJobs;
    }

	public int getMaxPreparedJobs() {
    	return maxPreparedJobs;
    }

	public void setMaxPreparedJobs(int maxPreparedJobs) {
    	this.maxPreparedJobs = maxPreparedJobs;
    }

	public int getDiskSpaceForVFS() {
    	return diskSpaceForVFS;
    }

	public void setDiskSpaceForVFS(int diskSpaceForVFS) {
    	this.diskSpaceForVFS = diskSpaceForVFS;
    }

	public boolean isUseVFSCache() {
    	return useVFSCache;
    }

	public void setUseVFSCache(boolean useVFSCache) {
    	this.useVFSCache = useVFSCache;
    }

	public int getMpiMachinesPerJob() {
    	return mpiMachinesPerJob;
    }

	public void setMpiMachinesPerJob(int mpiMachinesPerJob) {
    	this.mpiMachinesPerJob = mpiMachinesPerJob;
    }
/*
	public Set<String> getPreInstallBundleIds() {
    	return preInstallBundleIds;
    }

	public void setPreInstallBundleIds(Set<String> preInstallBundleIds) {
    	this.preInstallBundleIds = preInstallBundleIds;
    }
*/
	public OrderExecutionProfileInfo clone() {
	    OrderExecutionProfileInfo result = new OrderExecutionProfileInfo();
		result.launchMode = this.launchMode;
		result.cleanUpMode = this.cleanUpMode;
		
		result.maxJobRunningTime = this.maxJobRunningTime;
		result.maxRetries = this.maxRetries;
		result.maxConcurrentRunningJobs = this.maxConcurrentRunningJobs;
		result.maxPreparedJobs = this.maxPreparedJobs;
		result.diskSpaceForVFS = this.diskSpaceForVFS;

		result.useVFSCache = this.useVFSCache;
		result.mpiMachinesPerJob = this.mpiMachinesPerJob;
		
		return result;
    }
}
