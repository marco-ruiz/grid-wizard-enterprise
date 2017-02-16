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

import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.daemon.domain.AgentDomain;
import org.hibernate.annotations.GenericGenerator;


/**
 * @author Marco Ruiz
 * @since Aug 20, 2007
 */
@Entity
@GenericGenerator(name="ALLOC_SEQ", strategy="increment")
//@javax.persistence.SequenceGenerator(name="ALLOC_SEQ", initialValue=1000, allocationSize=1)
public class AllocationInfo extends BaseModelInfo<Integer> {

	private static Log log = LogFactory.getLog(AgentDomain.class);

	private static final int RELEASE_REASON_ERROR = 0; 
	private static final int RELEASE_REASON_TOO_LATE = 1; 
	private static final int RELEASE_REASON_TOO_IDLE = 2; 
	private static final int RELEASE_REASON_TOO_OLD = 3; 
	private static final int RELEASE_REASON_NON_RESPONSIVE = 4; 
	private static final int RELEASE_REASON_DAEMON_STOPPING = 5; 
	
    @Id 
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ALLOC_SEQ") 
    private int id;
	
	// Queuing Time
    protected String systemPid = null;
    
	// Allocation Time
    //@ManyToOne
    @Transient
    private ComputeResourceInfo compResource;

    private Timestamp whenScheduled = null;  // Queued in Job Manager
    private Timestamp whenAttained  = null;  // Assigned a compute node to it
    // Jobs are being prepared and processed cyclicly in between these 2 states
    private Timestamp whenReleased  = null;  // Mark for disposal. No more jobs should be prepared for this allocation. Waiting for current job to process to finish
    private Timestamp whenDisposed  = null;  // No more job to process or preparing. Disposed!
    
    private int releaseReason = -1;

    // Transients for real. Used while the allocation is alive (between attained and released)
    @Transient private transient Object lifeControllerLock = new Object();
    @Transient private transient JobExecutionInfo currentJobExecution = null;
	@Transient private transient DeathDealer deathDealer;
    @Transient private transient boolean registered = false;
    
    public AllocationInfo() {}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	// Queueing Time
	public String getSystemPid() {
        return systemPid;
    }

    public void setSystemPid(String pid) {
        systemPid = pid;
        whenScheduled = logEvent(EventType.ALLOCATION_SCHEDULED);
    }
	
	public boolean isRegistered() {
		return registered;
	}

	public Timestamp getWhenScheduled() { return whenScheduled; }
	public Timestamp getWhenAttained()  { return whenAttained; }
	public Timestamp getWhenReleased()  { synchronized (lifeControllerLock) { return whenReleased; } }
	public Timestamp getWhenDisposed()  { synchronized (lifeControllerLock) { return whenDisposed; } }
	
	public ComputeResourceInfo getCompResource() {
		return compResource;
	}
	
	public void setCompResource(ComputeResourceInfo compRes) {
		compResource = compRes;
		this.registered = true;
    	whenAttained = logEvent(EventType.ALLOCATION_ATTAINED);
	}
	
	public DeathDealer createDeathDealer(DaemonConfigDesc config) {
		if (deathDealer == null) deathDealer = new DeathDealer(config);
		return deathDealer;
	}
	
	public DeathDealer getDeathDealer() {
		return deathDealer;
	}
	
	public int getReleaseReason() {
		return releaseReason;
	}

	public void setReleaseReason(int reason) {
		releaseReason = reason;
		whenReleased = logEvent(EventType.ALLOCATION_RELEASED);
	}
	
	public JobExecutionInfo getProcessingExecution() {
		synchronized (lifeControllerLock) { return currentJobExecution; }
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
			while (currentJobExecution == null && whenReleased == null)
				try { lifeControllerLock.wait(); } catch (InterruptedException e) {} 

			return currentJobExecution;
		}
	}
	
	public boolean setProcessingExecution(JobExecutionInfo execution) {
		synchronized (lifeControllerLock) {
			if (whenReleased != null) return false;
			this.currentJobExecution = execution;
			execution.setAllocation(this);
			deathDealer.cancelIdleCountdown();
			lifeControllerLock.notifyAll();
			return true;
		}
	}

	public JobExecutionInfo dispose(boolean beatOverdue) {
		synchronized (lifeControllerLock) {
			if (whenReleased == null) 
				setReleaseReason((beatOverdue) ? RELEASE_REASON_NON_RESPONSIVE : RELEASE_REASON_ERROR);
			whenDisposed = logEvent(EventType.ALLOCATION_DISPOSED);
			lifeControllerLock.notifyAll();
			return currentJobExecution;
		}
	}
	
	public class DeathDealer implements Runnable {
		private long idleDeathTime = -1;
		private long oldDeathTime;
		private DaemonConfigDesc config;
		
		public DeathDealer(DaemonConfigDesc config) {
			this.config = config;
			this.oldDeathTime = whenAttained.getTime() + getHeadResource().getMaxHijackMillis();
        }

		public void startIdleCountdown()  {
			deathDealer.setIdleDeathTime(System.currentTimeMillis() + getHeadResource().getMaxIdleMillis());
		}
		
		public void cancelIdleCountdown() {
			deathDealer.setIdleDeathTime(-1);
		}
		
		private void setIdleDeathTime(long time) {
			synchronized (lifeControllerLock) {
				idleDeathTime = time;
				lifeControllerLock.notifyAll();			
			}
		}
		
		public void run() {
			// Verify the allocation will be disposed due to an allocation recycling strategy (age and/or lazyness)
			boolean hijack = getHeadResource().getMaxHijackMillis() > 0;
			if (!hijack && getHeadResource().getMaxIdleMillis() <= 0) return;
			
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
					if (whenReleased != null || whenDisposed != null) break;  // Externally disposed already
				}
				
				// Death dealer finalizing sequence
				if (whenReleased == null) {
					setReleaseReason((isIdleCutTime) ? RELEASE_REASON_TOO_IDLE : RELEASE_REASON_TOO_OLD);
					// Force agent API thread to realize this allocation has been released 
					// (when waiting for a processing job in getJobProcessing)
					lifeControllerLock.notifyAll(); 
				}
			}
		}
		
		private HeadResourceInfo getHeadResource() {
	        return config.getHeadResource();
        }
	}
	
	public ModelSummary<Integer> createModelSummaryFor(EventType ev) {
		switch (ev) {
		case ALLOCATION_RELEASED:
			return new ModelSummary<Integer>(this, releaseReason);

		default:
			return super.createModelSummaryFor(ev);
		}
	}

	public String getAgentBaseFileName(String suffix) {
    	return "agent-" + getId() + suffix;
    }
	
	public String getAgentScriptFileName() {
    	return getAgentBaseFileName(".sh");
    }
	
	public String getAgentSubmitFileName() {
    	return getAgentBaseFileName(".submit");
    }
	
    public String toString() {
		return "allocId:" + getId();
	}

    //=====================
    // "TOO LATE" FEATURE
    // TODO: Clean up
    //=====================

    @Transient private transient Timer tooLateTimer = null;
    @Transient private transient Boolean tooLateTimerRunning = true;
    @Transient private transient Object tooLateTimerLock = new Object();
	
	public void setTooLateTimerTask(TimerTask tooLateTimerTask, long delay) {
		this.tooLateTimer = new Timer();
		tooLateTimer.schedule(tooLateTimerTask, delay);
    }

	public boolean isTooLate() {
		synchronized (tooLateTimerLock) {
			cancelTooLateTimer();
			return (releaseReason == RELEASE_REASON_TOO_LATE);
        }
	}

	public void flagAsTooLate() {
		synchronized (tooLateTimerLock) {
			if (tooLateTimerRunning) {
				setReleaseReason(RELEASE_REASON_TOO_LATE);
				log.warn("Allocation " + id + " timeout waiting for it to be established!");
			}
			cancelTooLateTimer();
		}
	}

	private void cancelTooLateTimer() {
	    if (tooLateTimer != null) {
	    	tooLateTimer.cancel();
	    	tooLateTimer = null;
	    }
		tooLateTimerRunning = false;
    }
}
