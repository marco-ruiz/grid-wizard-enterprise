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

import org.gwe.integration.slicer.model.param.PMInt;
import org.gwe.integration.slicer.model.param.PMString;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class ExecBaseRegistrationModel extends ExecBaseGWEModel {

	public ExecBaseRegistrationModel(String title, String description) {
		this(title, description, "20");
	}
	
	public ExecBaseRegistrationModel(String title, String description, String defaultIterations) {
		super(title, "Registration", description);
		
		inputGroupModel.addParam(new PMString("Fixed Filename Filter", "fixedFileFilter", "Fixed image set"));
		inputGroupModel.addParam(new PMString("Moving Filename Filter", "movingFileFilter", "Moving image set"));
		
		outputGroupModel.addParam(new PMString("Remote Output Directory", "remoteOutputDirectory", "Directory for the output resampled images"));
		
		algorithmGroupModel.addParam(new PMInt("Histogram Bins", "histogramBins", "Histogram Bins", "30"));
		algorithmGroupModel.addParam(new PMInt("Spatial Samples", "spatialSamples", "Spatial Samples", "10000"));
		algorithmGroupModel.addParam(new PMString("Iterations", "iterations", "Iterations", defaultIterations));
	}
}
