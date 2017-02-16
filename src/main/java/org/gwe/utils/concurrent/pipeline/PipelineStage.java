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

package org.gwe.utils.concurrent.pipeline;

import java.util.concurrent.ExecutorService;

import org.gwe.utils.concurrent.ThreadPoolUtils;

/**
 * @author Marco Ruiz
 * @since Sep 3, 2008
 */
public abstract class PipelineStage<TARGET_TYPE> implements Runnable {
	
	private BlockingList<TARGET_TYPE> targets;
	
	private ExecutorService threadPool; 
	private PipelineStageProcessor<TARGET_TYPE> stageProcessor;
	private PipelineStage<TARGET_TYPE> nextPipeline = null;
	
	public PipelineStage(String name, int size, PipelineStageProcessor<TARGET_TYPE> stageProcessor) {
	    this.threadPool = ThreadPoolUtils.createThreadPool("Pipeline Processor Stage: " + name);
	    this.targets = new BlockingList<TARGET_TYPE>(size);
	    this.stageProcessor = stageProcessor;
    }
	
	public BlockingList<TARGET_TYPE> getTargets() {
    	return targets;
    }

	public PipelineStage<TARGET_TYPE> getNextPipeline() {
    	return nextPipeline;
    }
	
	public void setNextPipeline(PipelineStage<TARGET_TYPE> nextPipeline) {
    	this.nextPipeline = nextPipeline;
    }
	
	public void start() {
		runAsync(this);
		if (nextPipeline != null) nextPipeline.start();
	}
	
	public final void run() {
		while (true) {
	        try {
	            runAsync(new StageProcessorWrapper());
            } catch (Exception e) {}
        }
	}
	
	private void runAsync(Runnable runnable) {
		threadPool.submit(runnable);
    }
	
	class StageProcessorWrapper implements Runnable {
		
		private TARGET_TYPE target;
		
		public StageProcessorWrapper() throws InstantiationException, IllegalAccessException {
	        this.target = targets.extractNextPendingTargetBlocking();
        }
		
		public void run() {
			stageProcessor.process(target);
			try {
				targets.targetProcessed();
	            nextPipeline.getTargets().queueTarget(target);
            } catch (Exception e) {}
        }
	}
}

