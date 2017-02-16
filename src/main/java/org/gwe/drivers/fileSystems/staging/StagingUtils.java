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

import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since May 8, 2008
 */
public class StagingUtils {

	private static final String STAGING_VFS_SUBDIR = "staging-vfs";

	public static String getLocalFileName(String workspace, String remoteFile, String newName) {
		if (newName != null && !newName.equals("")) 
			remoteFile = IOUtils.concatenatePaths(IOUtils.getFilePath(remoteFile), newName);
		
		return IOUtils.concatenatePaths(workspace, STAGING_VFS_SUBDIR, remoteFile.replace("://", "/"));
	}
	
	public static void main(String[] args) {
		System.out.println(getLocalFileName("workspace/", "/path/myfile", "whatever"));
		System.out.println(getLocalFileName("workspace", "/path/myfile", "whatever"));
		System.out.println(getLocalFileName("workspace", "sftp://host/path/myfile", "whatever"));
	}
}

