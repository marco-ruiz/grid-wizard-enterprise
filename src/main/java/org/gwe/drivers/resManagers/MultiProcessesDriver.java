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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.AllocationInfo;

/**
 * @author Marco Ruiz
 * @since Sep 5, 2008
 */
public class MultiProcessesDriver extends CommandLineResourceManagerDriver {

	private static Log log = LogFactory.getLog(MultiProcessesDriver.class);

	public boolean isSupportedJobManagerAvailable() { return true; }

	public MultiProcessesDriver() { super("ls", "sh", "rm-processes.vm"); }

	public String queueAllocationRequest(final AllocationInfo alloc) throws ResourceAllocationException {
		// TODO: Fix this hack with a thread pool, better resolution for the process id and such
		new Thread(new Runnable() {
			public void run() {
				try {
	                MultiProcessesDriver.super.queueAllocationRequest(alloc);
                } catch (ResourceAllocationException e) {
                }
            }
		}).start();
		return "";
    }

    protected String extractIdFromSubmissionOutput(String[] results) {
    	if (results.length < 1) return "";
		return results[results.length - 1];
    }

    public AllocationPhase killAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return null;
    }

    public AllocationPhase checkAllocation(AllocationInfo alloc) throws ResourceAllocationException {
        return null;
    }
}

