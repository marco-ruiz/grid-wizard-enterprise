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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.persistence.model.AllocationInfo;

/**
 * @author Neil Jones
 * @author Marco Ruiz
 * @since Mar 15, 2007
 */
public class CondorDriver extends CommandLineResourceManagerDriver {

	private static Log log = LogFactory.getLog(CondorDriver.class);
	
	public static final String CONDOR_SUBMIT_FILE = "agent.condor.submit";

	private static String idPrefixToken = "submitted to cluster ";
	private static Pattern pattern = Pattern.compile(idPrefixToken + "(\\d+).");
	private static Map<String, AllocationPhase> statusFlags = new HashMap<String, AllocationPhase>();
    static {
    	statusFlags.put("I", AllocationPhase.SCHEDULED);
    	statusFlags.put("R", AllocationPhase.ATTAINED);
    	statusFlags.put("X", AllocationPhase.RELEASED);
    	statusFlags.put("C", AllocationPhase.RELEASED);
    }
    
	public CondorDriver() { super("condor_version", "condor_submit", "rm-condor.vm"); }

    protected String extractIdFromSubmissionOutput(String[] results) {
		String all = "";
		for (String line : results) all += line;
		log.info("Queueing results:\n" + all);
		
		String subResult = all.substring(all.lastIndexOf(idPrefixToken));
		Matcher matcher = pattern.matcher(subResult);
		return (matcher.matches()) ? matcher.group(0) : null;
    }

    public AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        String condorId = alloc.getSystemPid();
        if (condorId == null) return AllocationPhase.RELEASED;

        try {
			new ShellCommand("condor_rm " + condorId).runLocally();
		} catch (ConnectorException e) {
			throw new ResourceAllocationException("Failed to kill allocation '" + alloc.getId() + "'", e);
		}
        return checkAllocation(alloc);
    }

    public AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        String condorId = alloc.getSystemPid();
        if (condorId == null) return AllocationPhase.RELEASED;

		try {
	        AllocationPhase res = handleCondorOutput("condor_q", condorId);
	        return (res != null) ? res : handleCondorOutput("condor_history", condorId);
		} catch (ConnectorException e) {
			throw new ResourceAllocationException("Failed to check system level status of allocation '" + alloc.getId() + "'", e);
		}
    }

    private AllocationPhase handleCondorOutput(String condorCmd, String condorId) throws ConnectorException {
        String results = new ShellCommand(condorCmd + " " + condorId).runLocally();
    	AllocationPhase res = null;
        for(String line : results.split("\n")) {
            String[] chunks = line.split("\\s+");
            if (chunks.length >= 9 && chunks[0].equals(condorId + ".0")) 
            	res = statusFlags.get(chunks[5]);
        }
        log.trace("Condor job " + condorId + " has status " + res);
        return res;
    }
}

