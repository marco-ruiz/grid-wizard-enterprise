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

package org.gwe.p2elv2.functions;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.fileSystems.GridFileSystemUtils;
import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValueSpace;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.KeyStore;

/**
 * @author Marco Ruiz
 * @since May 1, 2008
 */
public class PFDir extends PFunction {

	private static Log log = LogFactory.getLog(PFDir.class);

	public PFDir() { super("dir"); }
	
    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
    	List<String> branchPatterns = new ArrayList<String>(params);
    	String path = branchPatterns.remove(0);
    	
    	List<String> matchingPaths = new ArrayList<String>();
    	matchingPaths.add(path);
    	for (String pattern : branchPatterns)
	        matchingPaths = findChildrenMatchingPatternNoException(matchingPaths, pattern, ctx.getKeys());
    	
    	return new PVarValueSpace(matchingPaths);
    }

    private List<String> findChildrenMatchingPatternNoException(List<String> paths, String pattern, KeyStore keys) {
    	try {
	        return findChildrenMatchingPattern(paths, pattern, keys);
        } catch (Exception e) {
        	log.info(e);
        }
        return new ArrayList<String>();
    }
	
    private List<String> findChildrenMatchingPattern(List<String> paths, String pattern, KeyStore keys) throws URISyntaxException, HandleOperationException {
    	List<String> result = new ArrayList<String>();
        Pattern patternObj = Pattern.compile(pattern);
    	for (String path : paths) {
	        for (String child : listChildren(path, keys)) {
	        	Matcher matcher = patternObj.matcher(child);
	        	if (matcher.matches()) 
	        		result.add(IOUtils.concatenatePaths(path, child));
	        }
        }
    	
	    return result;
    }
    
    public List<String> listChildren(String path, KeyStore keys) throws URISyntaxException, HandleOperationException {
		List<String> result = new ArrayList<String>();
		FileHandle root = keys.createFileLink(path).createHandle();
		if (root.isDirectory()) {
			for (FileHandle handle : root.getChildren()) {
		        result.add(IOUtils.getFileName(handle.getPath()));
		        GridFileSystemUtils.cleanUpHandle(handle);
	        }
		}
		return result;
    }
}

