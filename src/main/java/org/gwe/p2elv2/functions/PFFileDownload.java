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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gwe.drivers.bundleManagers.BundleHandle;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.drivers.fileSystems.staging.FileStagingException;
import org.gwe.drivers.fileSystems.staging.StagingUtils;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.p2elv2.PFunctionRuntime;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Aug 4, 2008
 */
public class PFFileDownload extends PFunctionRuntime {
	
	public PFFileDownload() { super("in"); }
    
    public PVarValue calculateValue(List<String> params, PStatementContext ctx) {
    	String newName = (params.size() > 1) ? params.get(1) : null;
		String remoteFile = params.get(0);
		final String localFile = StagingUtils.getLocalFileName(ctx.getWorkspace(), remoteFile, newName);
    	final BundleType bundleType = (params.size() == 3) ? extractBundleType(localFile) : null;
		
		final Map<String, String> downloads = new HashMap<String, String>();
		downloads.put(remoteFile, localFile);
		
		PProcessorStager downloadProcessor = new PProcessorStager(ctx.getStager()) {
			public void process() throws FileStagingException {
	    		stager.downloadFiles(downloads);
	    		if (bundleType == null) return;
	    		try {
	                decompressContents(localFile, bundleType);
                } catch (ConnectorException e) {
                	throw new FileStagingException("Problems decompressing file " + localFile, e);
                }
            }
		};
		
		return new PVarValue(localFile, downloadProcessor, null);
    }
    
	private void decompressContents(final String localFile, final BundleType bundleType) throws ConnectorException {
		// Create contents directory
		String contentsDir = localFile + "-contents";
		new File(contentsDir).mkdirs();
		
		// Create temporary copy (obfuscated) of compressed file under contents directory
		String tmpLocalFile = IOUtils.concatenatePaths(contentsDir, UUID.randomUUID().toString() + "-" + IOUtils.getFileName(localFile));
		ShellCommand.runLocally(ShellCommand.copyFile(localFile, tmpLocalFile));
		
		// Decompress temporary compressed file where it is located (contents directory)
    	try {
    		new BundleHandle(bundleType, tmpLocalFile).getUnbundleShellCommand().runLocally();
    	} catch (ConnectorException e) {}
		
		// Delete temporary compressed file
		ShellCommand.runLocally(ShellCommand.delete(tmpLocalFile));
    }

	private BundleType extractBundleType(String fileName) {
		for (BundleType bType : BundleType.values())
            if (!bType.equals(BundleType.NONE) && fileName.endsWith(bType.getExtension())) 
            	return bType;

        return null;
    }
}

