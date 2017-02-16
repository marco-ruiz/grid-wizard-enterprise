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

package org.gwe.drivers.fileSystems;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.services.BrokeredService;
import org.gwe.utils.services.PlainService;

/**
 * @author Marco Ruiz
 * @since Sep 17, 2008
 */
public class FileTransferer extends BrokeredService<Long> {

	private static Log log = LogFactory.getLog(FileTransferer.class);

	public FileTransferer(int maxParallelRequest, String name) {
	    super(maxParallelRequest, name + " File Transferer Service");
    }

	public Long transferFile(final String srcFile, final String destFile, final KeyStore keys) throws Exception {
		return processRequestBlocking(new PlainService<Long>() {
			public Long runService() throws Exception {
				return transferFileSync(srcFile, destFile, keys);
			}
		});
	}

	public long transferFileSync(String srcFile, String destFile, KeyStore keys) throws Exception {
		log.info("Transfering file from '" + srcFile + "' to '" + destFile + "'"); 
		
		FileHandle srcFileHandle = null;
		FileHandle destFileHandle = null;
		try {
			srcFileHandle  = keys.createFileLink(srcFile).createHandle();
			try {
				if (!srcFileHandle.exists()) return 0;
			} catch(Exception e) { 
				// Ignore this precautionary verification
			}
			destFileHandle = keys.createFileLink(destFile).createHandle();
			srcFileHandle.copyTo(destFileHandle);
			
			log.info("File transfer operation completed! ('" + srcFile + "' --> '" + destFile + "')");
			return GridFileSystemUtils.computeSize(srcFileHandle, destFileHandle);
		} catch(Exception e) {
			throw e;
		} finally {
			GridFileSystemUtils.cleanUpHandle(srcFileHandle);
			GridFileSystemUtils.cleanUpHandle(destFileHandle);
		}
	}
}

