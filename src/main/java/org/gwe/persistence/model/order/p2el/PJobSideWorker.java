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

package org.gwe.persistence.model.order.p2el;

import java.util.concurrent.Callable;

import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.PProcessor;
import org.gwe.p2elv2.PProcessorType;
import org.gwe.persistence.model.order.JobSideWorker;

/**
 * @author Marco Ruiz
 * @since Aug 20, 2008
 */
public abstract class PJobSideWorker extends JobSideWorker {

	public PJobSideWorker(PPermutation perm, PProcessorType procType) {
    	for (PProcessor proc : perm.getProcessors(procType)) 
    		addCallableForProcessor(proc);
    }
    
	private void addCallableForProcessor(final PProcessor proc) {
		add(new Callable<Void>() {
        	public Void call() throws Exception {
        		proc.process();
                return null;
            }
        });
    }
}

