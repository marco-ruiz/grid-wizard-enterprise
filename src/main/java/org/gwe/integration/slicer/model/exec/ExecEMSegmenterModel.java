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

package org.gwe.integration.slicer.model.exec;

import org.gwe.integration.slicer.model.param.PMString;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class ExecEMSegmenterModel extends ExecBaseGWEModel {

	public ExecEMSegmenterModel() {
		super(
				"EM Segmentation", "Segmentation", 
				"A command-line executable that performs EM Segmentation on a compute cluster.\n" +
				" This is targeted to processing large numbers of images simultaneously."
		);
		
		inputGroupModel.addParam(new PMString("Filename Filter", "fileFilter", "Filename filter (these are moving images)"));
		outputGroupModel.addParam(new PMString("Remote Output Directory", "remoteOutputDirectory", "string", "Directory for the output transforms"));
	}
}
