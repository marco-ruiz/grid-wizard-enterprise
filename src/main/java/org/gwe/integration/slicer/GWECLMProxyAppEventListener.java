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

package org.gwe.integration.slicer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.EventListener;
import org.gwe.api.event.Event;
import org.gwe.persistence.model.ModelSummary;

/**
 * @author Marco Ruiz
 * @since Jan 22, 2008
 */
public class GWECLMProxyAppEventListener extends EventListener {

	private static Log log = LogFactory.getLog(GWECLMProxyAppEventListener.class);

	private static final int NUM_PROGRESS_EVENTS = 1;
	private static final int PROGRESS_BAR_LENGTH = 50;
	private static final float PROGRESS_BAR_PERCENTAGE_UNIT = (float) (100.0 / PROGRESS_BAR_LENGTH);
	private static final String FILTER_NAME = "GWECLMProxyApp";
	
	private int oId = -1;
	private float stepPercentageIncrement;
	private Map<Integer, Integer> progress = new HashMap<Integer, Integer>();
	private boolean liveReporting = false;
	private long stepStartTime;
	private boolean started = false;
	
	public GWECLMProxyAppEventListener() {
		stepStartTime = System.currentTimeMillis();
		setNumJobs(1);
	}

	public void reportProgressFor(int orderId) {
		if (liveReporting) throw new RuntimeException("Cannot change order to monitor!!!");
		oId = orderId;
		liveReporting = true;
		outputStartProgressMsg(0);
	}

	public void eventPerformed(Event ev) {
		super.eventPerformed(ev);
		
		List<ModelSummary> modelIds = ev.getModelIdentifiers();
		switch (ev.getEventType()) {
			case ORDER_EXPANDED:
				setNumJobs(ev.getModelIdentifiers().size() - 1);
				break;
			
			case JOB_COMPLETED:
				logStep(modelIds, 2);
				break;
				
			case ORDER_COMPLETED:
				if (logStep(modelIds, 0)) 
					exit();
				break;
		}
	}

	private void setNumJobs(int numJobs) {
	    stepPercentageIncrement = (float) (100.0 / (numJobs * NUM_PROGRESS_EVENTS));
    }
	
	private boolean logStep(List<ModelSummary> modelIds, int index) {
		ModelSummary orderSummary = modelIds.get(index);
		int orderId = Integer.parseInt(orderSummary.getKey().toString());
        int pSteps = getProgressSteps(orderId);
        progress.put(orderId, progress.get(orderId) + 1);
        if (oId != orderId) return false;
        outputEndProgressMsg(pSteps);
        stepStartTime = System.currentTimeMillis();
        pSteps++;
        outputStartProgressMsg(pSteps);
        return true;
	}

	private void outputEndProgressMsg(int pSteps) {
		outputProgress(" <filter-end>" + createFilterName(pSteps) + "<filter-time>" + (System.currentTimeMillis() - stepStartTime) + "</filter-time></filter-end>");
    }

	private void outputStartProgressMsg(int pSteps) {
	    int pPercent = Math.min((int)(pSteps * stepPercentageIncrement), 100);
        if (pPercent >= 100) 
        	exit();
        outputProgress("<filter-start>" + createFilterName(pSteps) + createFilterComment(pPercent) + "</filter-start>");
    }

	private int getProgressSteps(int orderId) {
		Integer prog = progress.get(orderId);
		if (prog == null) {
			prog = 0;
			progress.put(orderId, prog);
		}
		return prog;
	}

	private String createFilterName(int pSteps) {
		return "<filter-name>" + FILTER_NAME + pSteps + "</filter-name>";
	}
	
	private String createFilterComment(int pPercent) {
		String result = "<filter-comment>";
		int barUnitsCompleted = (int)(pPercent / PROGRESS_BAR_PERCENTAGE_UNIT);
		int barUnitsIncompleted = PROGRESS_BAR_LENGTH - barUnitsCompleted;
		for (int count = 0; count < barUnitsCompleted; count++) result += ">";
		result += " Grid Execution Progress "; 
		for (int count = 0; count < barUnitsIncompleted; count++) result += ">";
		return result + "</filter-comment>";
	}
	
	private void outputProgress(String progress) {
		System.out.print(progress);
		log.info("Order Progress: " + progress);
	}
	
	private void exit() {
        outputProgress("<filter-start>FINISH<filter-comment> Grid Execution Completed!</filter-comment></filter-start><filter-end>FINISH<filter-time>0</filter-time></filter-end>");
		System.exit(0);
	}
}
