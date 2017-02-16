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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.CompressedObject;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author Marco Ruiz
 * @since Sep 15, 2008
 */
@GenericGenerator(name = "jobExecutionInfoIdGenerator", strategy = "org.gwe.persistence.model.JobExecutionInfoIdGenerator")
@Entity
public class JobExecutionInfo extends BaseModelInfo<String> {
	
	private static Log log = LogFactory.getLog(JobExecutionInfo.class);
	
	// Creation Time
	@Id
	@GeneratedValue(generator = "jobExecutionInfoIdGenerator")
	private String id;
	
	@ManyToOne
	private JobInfo job;

	private int executionNum;
	
	// Schedule Time
	@ManyToOne
	protected HeadResourceInfo runningDaemon;

	// Schedule Time
	@ManyToOne
	protected AllocationInfo allocation = null;

//	private Timestamp whenStarted    = null; // Allocation is ready and started working on the job - CREATED!!!
	private Timestamp whenPrepared   = null; // Prepared in the compute resource assigned (file transfers complete and such)
	private Timestamp whenAssigned   = null; // Assigned a compute resource (allocation) for processing
	private Timestamp whenDispatched = null; // Dispatched to a compute resource / agent for processing
	private Timestamp whenProcessed  = null; // Processed by compute resource / agent
	private Timestamp whenCompleted  = null; // Compute resource cleaned up and job closed!

	private Timestamp whenFailed     = null; // This job was aborted. Reasons: user request, preparing error, ...

	@Lob @Column(length = 327680)
	private CompressedObject compressedResult = new CompressedObject(); // Meaningful only when whenCompleted != null

	@Lob @Column(length = 327680)
	private CompressedObject<Throwable> compressedException = new CompressedObject<Throwable>(); // Meaningful only when whenFailed != null

	public JobExecutionInfo() {}
	
    public JobExecutionInfo(JobInfo job) {
    	this.job = job;
        this.executionNum = job.getFailures() + 1;
        this.job.setExecution(this);
    }

	public String getId() {
	    return id;
    }

	public JobInfo getJob() {
    	return job;
    }

	public void setJob(JobInfo job) {
    	this.job = job;
    }

	public void setExecutionNum(int executionNum) {
	    this.executionNum = executionNum;
    }

	public int getExecutionNum() {
	    return executionNum;
    }

	public HeadResourceInfo getRunningDaemon() {
		return runningDaemon;
	}

	public void setRunningDaemon(HeadResourceInfo runningDaemon) {
		this.runningDaemon = runningDaemon;
	}

	public AllocationInfo getAllocation() {
		return allocation;
	}
	
	public boolean hasFailed() {
		return whenFailed != null;
	}
	
	public boolean hasCompleted() {
		return whenCompleted != null;
	}

	public void setAllocation(AllocationInfo alloc) {
		allocation = alloc;
		whenAssigned = logExecutionEvent(EventType.JOB_ASSIGNED);
	}

	public void flagAsDispatched() {
		whenDispatched = logExecutionEvent(EventType.JOB_DISPATCHED);
	}

	public Serializable getRequestResult() {
        return compressedResult.getTargetBlindly();
	}

	public void setRequestResult(Serializable result) {
        compressedResult.setTargetBlindly(result);
    }
	
	public Throwable getRequestException() {
        return compressedException.getTargetBlindly();
	}
	
	public String getRequestExceptionStackTrace() {
		Throwable exception = getRequestException();
		if (exception == null) return null;
		Writer result = new StringWriter();
		exception.printStackTrace(new PrintWriter(result));
		return result.toString();
	}

	public void setRequestException(Throwable result) {
        compressedException.setTargetBlindly(result);
    }
	
	public Timestamp getWhenPrepared()   { return clone(whenPrepared);   }
	public Timestamp getWhenAssigned()   { return clone(whenAssigned);   }
	public Timestamp getWhenDispatched() { return clone(whenDispatched); }
	public Timestamp getWhenProcessed()  { return clone(whenProcessed);  }
	public Timestamp getWhenCompleted()  { return clone(whenCompleted);  }
	public Timestamp getWhenFailed()     { return clone(whenFailed);     }

	public String getStatus() {
    	if (whenCompleted  != null) return "COMPLETED";
    	if (whenFailed     != null) return "FAILED";
    	if (whenDispatched != null) return "RUNNING";
	    return "WAITING";
    }
	
	private Timestamp clone(Timestamp source) {
		return (source != null) ? new Timestamp(source.getTime()) : null;
	}
	
	public void logExecutionPhase(EventType evType, Serializable result) {
		setRequestResult(result);
		Timestamp evTime = logExecutionEvent(evType);
		switch (evType) {
			case JOB_PREPARED  : whenPrepared  = evTime; break;
			case JOB_PROCESSED : whenProcessed = evTime; break;
			case JOB_COMPLETED : whenCompleted = evTime; break;
		}
	}

	public void logFailure(Throwable result) {
	    setRequestException(result);
	    whenFailed = logExecutionEvent(EventType.JOB_FAILED);
	    log.warn("Job execution '" + id + "' failed!", (Exception)result);
    }

	private Timestamp logExecutionEvent(EventType evType) {
//		OrderInfo or = job.getOrder();
		Timestamp result = logEvent(evType, job, job.getOrder());
/*
		if (evType.equals(EventType.JOB_COMPLETED) || evType.equals(EventType.JOB_ABORTED))
			or.incrementJobsFinished();
*/
		return result;
	}
}
