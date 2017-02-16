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

import java.util.HashMap;
import java.util.Map;

import org.gwe.utils.StringUtils;

/**
 * @author Marco Ruiz
 * @since Feb 20, 2008
 */
public class FilesTransferDescriptor {
	
	private Map<String, String> repo = new HashMap<String, String>();
	
	public FilesTransferDescriptor(String prefixDir, String[] potentialTransfers, String workspace) {
		for (String pTransfer : potentialTransfers)
			if (pTransfer.startsWith(prefixDir)) {
				String[] pieces = StringUtils.splitBoundaries(pTransfer, prefixDir + "[", "]:");
				String newName = ((pieces != null && pieces.length > 4 && pieces[2] != null) ? pieces[2] : null);
//				repo.put(pTransfer, getLocalFileName(workspace, pieces[4], newName));
				repo.put(pTransfer, StagingUtils.getLocalFileName(workspace, getURI(pTransfer), newName));
			}
    }

	public Map<String, String> getFileTransfers(boolean asURIs) {
		Map<String, String> result = new HashMap<String, String>();
		for (String remoteFile : repo.keySet()) 
			if (asURIs)
				result.put(getURI(remoteFile), repo.get(remoteFile));
			else
				result.put(" " + remoteFile, " " + repo.get(remoteFile));
				
		return result; 
	}

	private String getURI(String remoteFile) {
		return remoteFile.substring(remoteFile.indexOf(':') + 1);
	}
}

