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

package org.gwe.drivers.fileSystems.staging;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Feb 20, 2008
 */
class DownloadHandle implements Comparable<DownloadHandle> {

    private static Log log = LogFactory.getLog(DownloadHandle.class);

    static long CANNOT_FREE_NOW_SIZE = -1;
	
	String localFile;
	private FilesStager stager;
	
	private Future<Long> result;
	private DownloadHandleRepo repo;
	
	private long size = CANNOT_FREE_NOW_SIZE;

	private Long lastTimeAccessed = System.currentTimeMillis();
	private int clients = 0;
	private Boolean garbageCollected = false;

	DownloadHandle(String downloadedFile, FilesStager stager, Future<Long> future, DownloadHandleRepo repo) {
		this.localFile = downloadedFile;
		this.stager = stager;
		this.result = future;
		this.repo = repo;
	}

	public String getLocalFileWhenReady() throws InterruptedException, ExecutionException {
		size = result.get();
		return localFile;
	}

	long garbageCollect(boolean disposeAssociatedFile) {
		// Unnecessary synchronization since it will always be invoke from recycling thread which already owns this lock
		synchronized (repo) {
			if (clients > 0) return CANNOT_FREE_NOW_SIZE;
			if (disposeAssociatedFile) stager.cleanUp(localFile);
			log.info("Garbage collecting download: " + this.toString());
			garbageCollected = true;
			return size;
		}
	}
	
	public boolean lock() {
		synchronized (repo) {
			if (garbageCollected) return false;
			clients++;
			return true;
		}
	}

	public void unlock() {
		synchronized (repo) {
			clients--;
			if (clients == 0) lastTimeAccessed = System.currentTimeMillis();
		}
	}

	public int compareTo(DownloadHandle other) {
		return (this.clients > 0) ? 1 : (int)(this.lastTimeAccessed - other.lastTimeAccessed);
	}

	public long getSize() {
		return size;
	}
	
	public String toString() {
		return localFile + "[" + clients + "-" + garbageCollected + "]=" + size + ";" + (System.currentTimeMillis() - lastTimeAccessed) / 1000 + " secs ago";
	}
}
