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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.order.OrderDescriptor;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author Marco Ruiz
 * @since Aug 3, 2007
 */
@Entity
@GenericGenerator(name="ORDER_SEQ", strategy="increment")
//@javax.persistence.SequenceGenerator(name="ORDER_SEQ", initialValue=1000, allocationSize=1)
public final class OrderInfo extends BaseModelInfo<Integer> {

	private static Log log = LogFactory.getLog(OrderInfo.class);
	
    @Id 
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ORDER_SEQ") 
    private int id;
    
    // Id of the order after which this one should run. The first one is 0.
    private int priority = 0;  

    private String description;
    private String email;
    
//  @ManyToOne(fetch=FetchType.EAGER)
    @Transient
    private GridInfo grid;
    
    @Lob 
    @Column(length=20480)
    private OrderDescriptor descriptor; 
    
    @Lob 
    @Column(length=4096)
    private Serializable specificParameters = null;
    
    private Timestamp whenCompleted = null;
    
    private boolean paused = false;
    private boolean aborted = false;
    private boolean deleted = false;
    private boolean failed = false;
    
    private int totalJobsCount = 0;
    private int completedJobsCount = 0;
    private int failedJobsCount = 0;

	@OneToMany
    private List<JobInfo> jobs;
    
    private Class<? extends ResultParser> resultParserClass = ResultParser.class;
    
    @OneToOne(cascade=CascadeType.ALL)
	private OrderExecutionProfileInfo executionProfile;

	public OrderInfo() {}
	
	public OrderInfo(OrderDescriptor descriptor) {
		this(descriptor, null);
	}

	public OrderInfo(OrderDescriptor descriptor, String email) {
		this("Order from " + email, descriptor, email);
	}
	
	public OrderInfo(String description, OrderDescriptor descriptor, String email) {
		this.description = description;
		this.descriptor = descriptor;
		this.email = email;
	}
	
	public Integer getId() {
		return id;
	}

	public String getUniversalId() {
		return 	getId() + "-" + getWhenCreated().getTime();
	}

	private void setId(int id) {
		this.id = id;
	}

	public int getPriority() {
    	return priority;
    }

	public void setPriority(int priority) {
    	this.priority = priority;
    }

	public GridInfo getGrid() {
		return grid;
	}

	public void setGrid(GridInfo grid) {
		this.grid = grid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public OrderDescriptor getDescriptor() {
		return descriptor;
	}
	
	public void setDescriptor(OrderDescriptor jobDescriptor) {
		this.descriptor = jobDescriptor;
	}
	
	public List<JobInfo> getJobs() {
		return jobs;
	}

	public String getWorkspaceInDaemon(DaemonConfigDesc config) {
		return config.getHeadResource().getInstallation().getOrdersWorkspacePath(getId());		
	}
	
	public void generateJobs(DaemonConfigDesc config, OrderExecutionProfileInfo executionProfile) {
		this.executionProfile = executionProfile;
		try {
			jobs = descriptor.generateJobs(config);
		} catch(Exception e) {
			log.warn("Order descriptor failed while generating jobs: '" + descriptor + "'", e);
			jobs = new ArrayList<JobInfo>();
			failed = true;
		}
		
		JobInfoIdGenerator jobIdInfoGenerator = new JobInfoIdGenerator();
		for (JobInfo job : jobs) {
			job.setOrder(this);
			job.getDescriptor().processSystemDependencies(config, job);
			job.getRequest().setMaxJobRunningTime(executionProfile.getMaxJobRunningTime());
		}
		totalJobsCount = jobs.size();
		logEvent(EventType.ORDER_EXPANDED, jobs.toArray(new BaseModelInfo[]{}));
		checkIfCompleted();
	}

	public String toCVS() {
		StringBuffer result = new StringBuffer("");
		List<String> varNames = descriptor.getVarNames();

		result.append("Run");
		for (String varName : varNames) 
	        result.append(",").append(varName);
		
		result.append("\n");
		for (JobInfo job : jobs)
			result.append("\"").append(job.getJobNum()).append("\",").append(job.getDescriptor().toCSV(varNames)).append("\n");

		return result.toString();
	}

	public String toCommands() {
		StringBuffer result = new StringBuffer("");
		for (JobInfo job : jobs)
			result.append(job.getDescriptor()).append("\n");

		return result.toString();
	}

	public String toThinModel() {
		String stmt = ((POrderDescriptor)descriptor).getP2ELStatement();
		
		for (JobInfo job : jobs) 
			job.getDescriptor().getPermutationValues();

		return "";//result.toString();
	}

	public Serializable getSpecificParameters() {
		return specificParameters;
	}

	public void setSpecificParameters(Serializable specificParameters) {
		this.specificParameters = specificParameters;
	}

	public ResultParser createResultParser() throws InstantiationException, IllegalAccessException {
		return resultParserClass.newInstance();
	}

	public Class<? extends ResultParser> getResultParserClass() {
		return resultParserClass;
	}

	public void setResultParserClass(Class<? extends ResultParser> resultParserClass) {
		this.resultParserClass = resultParserClass;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

    public boolean isDeleted() {
    	return deleted;
    }

	public void setDeleted(boolean deleted) {
    	this.deleted = deleted;
    }

	public String toString() {
		return getDescriptor() + "\nTo be executed by " + getGrid(); 
	}

	public Timestamp getWhenCompleted() {
		return whenCompleted;
	}

	public void setWhenCompleted(Timestamp whenCompleted) {
		this.whenCompleted = whenCompleted;
	}

	public void incrementJobsCompleted(int amount) {
		completedJobsCount += amount;
		log.info("More jobs completed for order " + id + ". Latest count: " + completedJobsCount);
		checkIfCompleted();
	}

	public void incrementJobsFailed(int amount) {
		failedJobsCount += amount;
		log.info("More jobs failed for order " + id + ". Latest count: " + failedJobsCount);
		checkIfCompleted();
	}

	private void checkIfCompleted() {
		if (isFinished()) whenCompleted = logEvent(EventType.ORDER_COMPLETED);
	}
	
	public boolean isFinished() {
		return completedJobsCount + failedJobsCount >= totalJobsCount;
	}

	public int getTotalJobsCount() {
		return totalJobsCount;
	}

	public int getCompletedJobsCount() {
		return completedJobsCount;
	}

    public int getFailedJobsCount() {
    	return failedJobsCount;
    }

    public OrderExecutionProfileInfo getExecutionProfile() {
    	return executionProfile;
    }

	public void setExecutionProfile(OrderExecutionProfileInfo executionProfile) {
    	this.executionProfile = executionProfile;
    }
}
