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

package org.gwe.utils.concurrent;

import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Marco Ruiz
 * @since Nov 7, 2007
 */
public class Heart<HEART_CONTAINER_TYPE> implements Runnable {

	private static Log log = LogFactory.getLog(Heart.class);

	private static ExecutorService heartsThreadPool = ThreadPoolUtils.createThreadPool("Hearts", true);

	private Object id;
	private HEART_CONTAINER_TYPE heartBody;
	private HeartBeater<HEART_CONTAINER_TYPE> beater;
	private int period;

	private long lastHeartBeat = System.currentTimeMillis();
	private BooleanLock pauseLock = new BooleanLock();
	private boolean mustStop = false;

	public Heart(Object id, HEART_CONTAINER_TYPE heartBody, HeartBeater<HEART_CONTAINER_TYPE> beater, int period) {
		this.id = id;
		this.heartBody = heartBody;
		this.beater = beater;
		this.period = period;
		heartsThreadPool.submit(this);
	}
	
    public void stop() { 
    	synchronized (pauseLock) { 
			log.trace("Stopping: [id=" + id + ", body=" + heartBody + "]"); 
    		mustStop  = true; 
    	}
    }

    public void pause() { 
    	synchronized (pauseLock) {
			log.trace("Pausing: [id=" + id + ", body=" + heartBody + "]"); 
    		pauseLock.applyLock(); 
    	}
    }

    public void resume() { 
    	synchronized (pauseLock) { 
			log.trace("Resuming: [id=" + id + ", body=" + heartBody + "]"); 
	    	pauseLock.releaseLock();
	    	resetLastHeartBeat();
    	}
    }

    public void resetLastHeartBeat() { 
    	synchronized (pauseLock) { lastHeartBeat = System.currentTimeMillis(); }
    }
    
	public void run() {
		long timeToNextBeat = 0;
		while (true) {
			synchronized (pauseLock) {
				if (mustStop) {
					log.trace("Disposing: [id=" + id + ", body=" + heartBody + "]"); 
					return;
				}
				pauseLock.waitUntilConditionTrue();
				log.trace("Pause lock released: [id=" + id + ", body=" + heartBody + "]"); 
				timeToNextBeat = lastHeartBeat + period - System.currentTimeMillis();
				if (timeToNextBeat <= 0) {
					log.trace("Heart beating: [id=" + id + ", body=" + heartBody + "]"); 
					if (beater == null || beater.beatAndReportIfMustContinue(id, heartBody)) resetLastHeartBeat();
					timeToNextBeat = period;
				}
			}
			
			try { Thread.sleep(timeToNextBeat); } catch (InterruptedException e) {}
		}
	}
}

