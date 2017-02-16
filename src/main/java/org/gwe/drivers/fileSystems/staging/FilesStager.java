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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.IOUtils;
import org.gwe.utils.concurrent.ThreadPoolUtils;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.services.BrokeredService;
import org.gwe.utils.services.PlainService;

/**
 * @author Marco Ruiz
 * @since Feb 19, 2008
 */
public class FilesStager {
	
    private static Log log = LogFactory.getLog(FilesStager.class);

	private static ExecutorService downloadersThreadPool = ThreadPoolUtils.createThreadPool("File Downloaders");

	// To prevent more than one command to be issued in parallel and reduce the chances to get the 
	// IOException "to many files opened"
	private static ShellCommandExecutor filesCleaner = new ShellCommandExecutor(1, "File Cleaner");
	private static ShellCommandExecutor filesMirrorer = new ShellCommandExecutor(10, "File Manager");

	private Map<String, DownloadHandle> downloadsCache = new HashMap<String, DownloadHandle>();
	private KeyStore keys;
	private String downloadFolder;
	
	public FilesStager(String downloadFolder, KeyStore keys) {
		this.downloadFolder = downloadFolder;
		this.keys = keys;
		IOUtils.createLocalFolder(downloadFolder);
	}

	public void downloadFiles(Map<String, String> fileTransfers) throws FileStagingException {
		// Schedule asynchronous downloads
		Map<String, DownloadHandle> handles = new HashMap<String, DownloadHandle>();
		log.info("Ready to request downloads: " + fileTransfers);
		for (String remoteFile : fileTransfers.keySet())
			handles.put(remoteFile, downloadFile(remoteFile));

//		DownloadHandleRepo.getInstance().startGarbageCollection();
		
		// Wait for asynchronous downloads and gather where they were downloaded
		Map<String, String> downloadLocations = new HashMap<String, String>();
		for (String remoteFile : handles.keySet()) {
			try {
				downloadLocations.put(remoteFile, handles.get(remoteFile).getLocalFileWhenReady());
			} catch (ExecutionException e) {
				throw new FileStagingException("Exception encountered while downloading file " + remoteFile, e.getCause());
			} catch (Exception e) {
				throw new FileStagingException("Exception encountered while downloading file " + remoteFile, e);
			}
		}
		
		// Copy downloaded files to the actual staging location where expected
		mirrorFiles(fileTransfers, handles, downloadLocations);

		handles.clear();
		downloadLocations.clear();
	}
	
	public DownloadHandle downloadFile(final String remoteFile) {
		DownloadHandle handle;
		synchronized (downloadsCache) {
			handle = downloadsCache.get(remoteFile);
			if (handle == null || !handle.lock()) {
				log.info("Cached of file '" + remoteFile + "' not found in virtual cache FS. Downloading it...");
				final String downloadedFile = StagingUtils.getLocalFileName(downloadFolder, remoteFile, "cache-" +  downloadsCache.size());
                Future<Long> future = downloadersThreadPool.submit(new Callable<Long>() {
                	public Long call() throws Exception {
                		return GWEAppContext.getGridFileSystem().stageFile(remoteFile, downloadedFile, keys);
                	}
                });
				handle = DownloadHandleRepo.getInstance().createHandle(downloadedFile, this, future);
				downloadsCache.put(remoteFile, handle);
			} else {
				log.info("Cached of file '" + remoteFile + "' found in virtual cache FS!");
			}
		}
		return handle;
	}
	
	private void mirrorFiles(Map<String, String> fileTransfers, Map<String, DownloadHandle> handles, Map<String, String> downloadLocations) throws FileStagingException {
		log.info("Downloads ready to be copied from cached virtual file system: " + downloadLocations);
		List<String[]> mirrors = new ArrayList<String[]>();
	    for (String remoteFile : fileTransfers.keySet()) {
			String src  = downloadLocations.get(remoteFile);
			String dest = fileTransfers.get(remoteFile);
			mirrors.add(new String[]{src, dest});
		}
	    
		List<String> cmds = new ArrayList<String>(mirrors.size() * 2);
	    for (String[] mirror : mirrors) {
	    	IOUtils.makeFilePath(mirror[1]);
	    	cmds.add(ShellCommand.copyFile(mirror[0], mirror[1]));
	    }
	    try {
    		filesMirrorer.executeShellCmd(cmds);
	    } catch (Exception e) {
	    	throw new FileStagingException("Problem encountered while copying mirroring files. " +
	    		"File Transfers: " + fileTransfers + "Download Locations: " + downloadLocations, e);
	    }
	    
	    for (String remoteFile : fileTransfers.keySet()) handles.get(remoteFile).unlock();
    }

	public void prepareUploadDirectories(Map<String, String> uploads) throws FileStagingException {
		try {
			for (String localFile : uploads.keySet()) IOUtils.makeFilePath(localFile);
        } catch (Exception e) {
			throw new FileStagingException("Problems encountered while creating temporary destinations for files to stage out: " + 
					uploads, e);
        }
	}

	public void uploadFiles(Map<String, String> uploads) throws FileStagingException {
		// Copy local files to the actual remote locations
		for (String localFile : uploads.keySet()) {
			String remoteFile = uploads.get(localFile);
			try {
				GWEAppContext.getGridFileSystem().uploadFile(localFile, remoteFile, keys);
			} catch (Exception e) {
				throw new FileStagingException("Exception encountered while uploading file '" + localFile + "' to '" + remoteFile + "'", e);
			}
		}
	}
	
	public void cleanUp(String workspacePath) {
//		if (true) return;
		if (workspacePath != null && !"".equals(workspacePath)) {
            try {
            	filesCleaner.executeShellCmd(ShellCommand.delete(workspacePath));
            } catch (Exception e) {
                // TODO Auto-generated catch block
            	log.warn("Couldn't clean up file system entry " + workspacePath, e);
            }
        }
    }

	public void dispose() {
		cleanUp(downloadFolder);
		DownloadHandleRepo.getInstance().disposeDownloadsUnder(downloadFolder);
    }
}

class ShellCommandExecutor extends BrokeredService<Void> {

	public ShellCommandExecutor(int maxParallelRequest, String name) {
	    super(maxParallelRequest, name + " Shell Command Executor");
    }
	
	public Void executeShellCmd(final List<String> cmds) throws Exception {
		return executeShellCmd(cmds.toArray(new String[]{}));
	}
	
	public Void executeShellCmd(final String... cmds) throws Exception {
		return processRequestBlocking(new PlainService<Void>() {
			public Void runService() throws Exception {
		        for (String cmd : cmds) ShellCommand.runLocally(cmd);
		        return null;
			}
		});
	}
}
