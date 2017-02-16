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

/**
 * @author Marco Ruiz
 * @since Dec 31, 2008
 */
public class AllocationLive implements Runnable {

	private final AllocationInfo alloc;
	private long idleDeathTime = -1;
	private long oldDeathTime;
	private HeadResourceInfo headResource;
	
    // Transients for real. Used while the allocation is alive (between attained and released)
    private Object lifeControllerLock = new Object();
    private JobExecutionInfo currentJobExecution = null;
    private boolean registered = false;

	public AllocationLive(AllocationInfo alloc, DaemonConfigDesc config) {
		this.alloc = alloc;
		this.headResource = config.getHeadResource();
		this.oldDeathTime = alloc.getWhenAttained().getTime() + headResource.getMaxHijackMillis();
    }
	
    public boolean isRegistered() {
    	return registered;
    }

	public void registerAsRunningOn(ComputeResourceInfo compRes) {
		registered = true;
		alloc.setCompResource(compRes);
	}

	public void startIdleCountdown()  {
		setIdleDeathTime(System.currentTimeMillis() + headResource.getMaxIdleMillis());
	}
	
	public void cancelIdleCountdown() {
		setIdleDeathTime(-1);
	}
	
	private void setIdleDeathTime(long time) {
		synchronized (lifeControllerLock) {
			idleDeathTime = time;
			lifeControllerLock.notifyAll();			
		}
	}
	
	public JobExecutionInfo getProcessingExecution() {
		synchronized (lifeControllerLock) { 
			return currentJobExecution; 
		}
	}
	
	public JobExecutionInfo extractProcessingExecution() {
		synchronized (lifeControllerLock) {
			JobExecutionInfo result = currentJobExecution;
			currentJobExecution = null;
			return result;
		}
	}
	
	public JobExecutionInfo getNextProcessingExecutionBlocking() {
		synchronized (lifeControllerLock) { 
			while (currentJobExecution == null && alloc.getWhenReleased() == null)
				try { lifeControllerLock.wait(); } catch (InterruptedException e) {} 

			return currentJobExecution;
		}
	}
	
	public boolean setProcessingExecution(JobExecutionInfo execution) {
		synchronized (lifeControllerLock) {
			if (alloc.getWhenReleased() != null) return false;
			
			currentJobExecution = execution;
			execution.setAllocation(alloc);
			cancelIdleCountdown();
			lifeControllerLock.notifyAll();
			return true;
		}
	}
/*
	public JobExecutionInfo dispose(boolean beatOverdue) {
		synchronized (lifeControllerLock) {
			if (alloc.getWhenReleased() == null)
				alloc.setReleaseReason((beatOverdue) ? AllocationReleaseReasons.NON_RESPONSIVE : AllocationReleaseReasons.ERROR);
			
			alloc.dispose();
			lifeControllerLock.notifyAll();
			return currentJobExecution;
		}
	}
*/
	public void run() {
		// Verify the allocation will be disposed due to an allocation recycling strategy (age and/or lazyness)
		boolean hijack = headResource.getMaxHijackMillis() > 0;
		if (!hijack && headResource.getMaxIdleMillis() <= 0) return;
		
		// Allocation is set for some form of recycling 
		boolean isIdleCutTime; 
		synchronized (lifeControllerLock) {
			// Death dealer cycle
			while (true) {
				isIdleCutTime = (0 <= idleDeathTime && idleDeathTime <= oldDeathTime);
				long remaining = (isIdleCutTime ? idleDeathTime : oldDeathTime) - System.currentTimeMillis();
				if (remaining <= 0) break; // Too idle or too old
				try { 
					lifeControllerLock.wait(remaining); 
				} catch (InterruptedException e) {
					// TODO: Release allocation due to interruption sent by daemon shutting down sequence ?
					// Or do this in "dispose" method from the allocation?
				}
				if (alloc.getWhenReleased() != null || alloc.getWhenDisposed() != null) break;  // Externally disposed already
			}
			
			// Death dealer finalizing sequence
/*
			if (alloc.getWhenReleased() == null) {
				alloc.setReleaseReason((isIdleCutTime) ? AllocationReleaseReasons.TOO_IDLE : AllocationReleaseReasons.TOO_OLD);
				// Force agent API thread to realize this allocation has been released 
				// (when waiting for a processing job in getJobProcessing)
				lifeControllerLock.notifyAll(); 
			}
*/
		}
	}
}