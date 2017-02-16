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

/**
 * @author Marco Ruiz
 * @since Jul 3, 2007
 */
public class GridFileSystem {

	private static Log log = LogFactory.getLog(GridFileSystem.class);

	private FileTransferer downloadfileTransferer;
	private FileTransferer uploadFileTransferer;

	public GridFileSystem(int maxParallelFileTransfers) {
		this.downloadfileTransferer = new FileTransferer(maxParallelFileTransfers, "Download");
		this.uploadFileTransferer   = new FileTransferer(5, "Upload");
	}

	public Long stageFile(String srcFile, String destFile, KeyStore keys) throws Exception {
		return downloadfileTransferer.transferFile(srcFile, destFile, keys);
	}

	public Long transferFile(String srcFile, String destFile, KeyStore keys) throws Exception {
		return downloadfileTransferer.transferFile(srcFile, destFile, keys);
	}

	public Long uploadFile(String srcFile, String destFile, KeyStore keys) throws Exception {
		return uploadFileTransferer.transferFile(srcFile, destFile, keys);
	}
}

