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
import org.gwe.drivers.HandleOperationException;

/**
 * @author Marco Ruiz
 * @since Sep 17, 2008
 */
public class GridFileSystemUtils {
	
	private static Log log = LogFactory.getLog(GridFileSystemUtils.class);

	// TODO: Make it more robust and don't just give up, especially with directories!
	public static long computeSize(FileHandle srcFileHandle, FileHandle destFileHandle) {
	    long result = 0;
	    try {
	    	result = destFileHandle.getSize();
	    } catch (Exception e) {
	    	try {
	    		result = srcFileHandle.getSize();
	    	} catch (Exception e2) {
	    	}
	    }
	    return result;
    }

	public static void cleanUpHandle(FileHandle handle) {
		try {
			if (handle != null) handle.close();
        } catch (HandleOperationException e) {
        	log.warn("Couldn't close file " + handle.getURI(), e);
        } finally {
            handle = null;
        }
	}
}
