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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Feb 20, 2008
 */
public class DownloadHandleRepo implements Runnable {

    private static Log log = LogFactory.getLog(DownloadHandleRepo.class);

	private static long BYTES_IN_GYGABYTE = 1024 * 1024 * 1024;
	
	private static DownloadHandleRepo instance = null;
	
	// TODO: Move to Spring factory
	public static DownloadHandleRepo getInstance() {
		if (instance == null) instance = new DownloadHandleRepo(0.1); // 100 MB
//		if (instance == null) instance = new DownloadHandleRepo(0.5); // Half a gigabyte
		return instance;
	}
	
	private Set<DownloadHandle> repo = new TreeSet<DownloadHandle>();
	private long repoSize;
	private int filesDownloadedSinceLastGC = 0;
	
	public DownloadHandleRepo(double gigabytes) {
		this.repoSize = (long)(gigabytes * BYTES_IN_GYGABYTE);
		new Thread(this, "BG Service - Virtual Cache FS Garbage Collector").start();
	}

	public DownloadHandle createHandle(String downloadedFile, FilesStager stager, Future<Long> future) {
		DownloadHandle handle = new DownloadHandle(downloadedFile, stager, future, this);
		synchronized (this) {
			repo.add(handle);
			filesDownloadedSinceLastGC++;
		}
		return handle;
	}
	
	public void startGarbageCollection() {
		synchronized (this) { this.notifyAll(); }
	}

	public void run() {
		while (true) {
			synchronized (this) {
				try {
					wait();
				} catch (InterruptedException e) {}
				garbageCollectAsNeeded();
			}
		}
	}

	private void garbageCollectAsNeeded() {
		long handleSize;
		long memoryCount = repoSize;
//		log.debug("Repository before garbage collection:\n" + repo);
		Set<DownloadHandle> removedHandles = new HashSet<DownloadHandle>();
		for (DownloadHandle handle : repo) {
			if (memoryCount > 0) {
				handleSize = handle.getSize();
				if (handleSize != DownloadHandle.CANNOT_FREE_NOW_SIZE) memoryCount -= handleSize;
			} else {
				handleSize = handle.garbageCollect(true);
				if (handleSize != DownloadHandle.CANNOT_FREE_NOW_SIZE) removedHandles.add(handle);
			}
			repo.removeAll(removedHandles);
		}
//		log.debug("Repository after garbage collection:\n" + repo);
		filesDownloadedSinceLastGC = 0;
	}

	public void disposeDownloadsUnder(String topLevelFolder) {
		synchronized (this) {
//			log.debug("Repository before disposing order virtual cache FS:\n" + repo);
//			deleteFileSystemEntry(topLevelFolder, account);
			Set<DownloadHandle> removedHandles = new HashSet<DownloadHandle>();
			for (DownloadHandle handle : repo) {
				if (handle.localFile != null && handle.localFile.startsWith(topLevelFolder)) {
					handle.garbageCollect(false);
					removedHandles.add(handle);
				}
			}
			repo.removeAll(removedHandles);
//			log.debug("Repository before disposing order virtual cache FS:\n" + repo);
		}
	}
}

