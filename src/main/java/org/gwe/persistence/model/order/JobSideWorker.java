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

package org.gwe.persistence.model.order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.gwe.persistence.model.live.JobLive;
import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Aug 19, 2008
 */
public abstract class JobSideWorker extends ArrayList<Callable<Void>> {
	
    private static ExecutorService threadPool = ThreadPoolUtils.createThreadPool("Job Side Workers");

    public final void execute(JobLive ctx) throws Throwable {
		beforeExecuteProcessors(ctx);
		if (!this.isEmpty()) {
			List<Future<Void>> results = threadPool.invokeAll(this);
			for (Future<Void> result : results) {
				try {
					result.get();
				} catch(ExecutionException e) {
					throw e.getCause();
				}
            }
		}
	}
	
	protected abstract void beforeExecuteProcessors(JobLive ctx) throws Exception;

}
