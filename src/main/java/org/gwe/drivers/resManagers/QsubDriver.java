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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.persistence.model.AllocationInfo;

/**
 * @author  Neil Jones
 * @since Mar 8, 2007
 */
public class QsubDriver extends CommandLineResourceManagerDriver {

	private static Log log = LogFactory.getLog(QsubDriver.class);
    private static Pattern pattern = Pattern.compile("Your job (\\d+) .* has been submitted");

	public QsubDriver() { super("qstat", "qsub", "rm-qsub.vm"); }

    protected String extractIdFromSubmissionOutput(String[] results) {
        Matcher matcher = pattern.matcher(results[0]);
        return (matcher.matches()) ? matcher.group(1) : null;
    }
	
    public AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException {
    	AllocationPhase stat = checkAllocation(alloc);
        if (stat == AllocationPhase.ATTAINED) {
        	try {
        		new ShellCommand("qdel " + alloc.getSystemPid()).runLocally();
			} catch (ConnectorException e) {
				throw new ResourceAllocationException("Failed to execute 'qdel' command to kill allocation '" + alloc.getId() + "'", e);
			}
            return AllocationPhase.RELEASED;
        }

        return stat;
    }

    public AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        String job_id = alloc.getSystemPid();
        if (null == job_id || "null".equals(job_id) || "".equals(job_id))
            throw new ResourceAllocationException("Job has no system-level ID; cannot check it.", null);

        String[] results;
		try {
			results = new ShellCommand("qstat -j " + job_id).runLocally().split("\n");  // + " 2>/dev/null | egrep \"job_number|error reason\"");
		} catch (ConnectorException e) {
			throw new ResourceAllocationException("Failed to execute 'qstat' command to check status of allocation '" + alloc.getId() + "'", e);
		}
        boolean finished = false;
        for(String line : results) {
            if (line.matches(".*job_number.*")) {
                finished = true;
            } else if (line.matches(".*error reason.*")) {
                return AllocationPhase.RELEASED;
            }
        }
        if (finished) return AllocationPhase.RELEASED;

        try {
			results = new ShellCommand("qacct -j " + job_id).runLocally().split("\n");
		} catch (ConnectorException e) {
			throw new ResourceAllocationException("Faile to execute 'qacct' command to check status of allocation '" + alloc.getId() + "'", e);
		}
        int start = -1;
        for (int idx = results.length - 1; idx >= 0; --idx) {
            if (results[idx].matches("=*")) {
                start = idx;
                break;
            }
        }

        if (start == -1)
            throw new ResourceAllocationException("Could not find that job in the list of running or completed jobs.", null);

        for (; start < results.length; ++start) {
            if (results[start].matches("exit_status\\s*0")) {
                return AllocationPhase.RELEASED;
            }
            else if (results[start].matches("exit_status")) {
                // any non-0 exit status is bad!
                return AllocationPhase.RELEASED;
            }
        }

        throw new ResourceAllocationException("Could not determine job status for " + job_id + " in SGE.", null);
    }
}
