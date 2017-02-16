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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwe.drivers.fileSystems.staging.FileStagingException;
import org.gwe.drivers.fileSystems.staging.StagingUtils;
import org.gwe.p2elv2.PFunctionRuntime;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;

/**
 * @author Marco Ruiz
 * @since Aug 4, 2008
 */
public class PFFileUpload extends PFunctionRuntime {

	public PFFileUpload() { super("out"); }
    
    public PVarValue calculateValue(List<String> params, PStatementContext ctx) {
    	String remoteFile = params.get(0);
		String localFile = StagingUtils.getLocalFileName(ctx.getWorkspace(), remoteFile, null);

		final Map<String, String> uploads = new HashMap<String, String>();
		uploads.put(localFile, remoteFile);
		
		PProcessorStager prepareDirectoriesProcessor = new PProcessorStager(ctx.getStager()) {
			public void process() throws FileStagingException {
	    		stager.prepareUploadDirectories(uploads);
            }
		};
		
		PProcessorStager uploadProcessor = new PProcessorStager(ctx.getStager()) {
			public void process() throws FileStagingException {
	    		stager.uploadFiles(uploads);
            }
		};
		
		return new PVarValue(localFile, prepareDirectoriesProcessor, uploadProcessor);
    }
}

