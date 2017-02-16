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

import org.gwe.integration.slicer.model.ExecutableModel;
import org.gwe.integration.slicer.model.GroupModel;
import org.gwe.integration.slicer.model.param.PMString;
import org.gwe.utils.cmd.OptionTemplate;
import org.gwe.utils.cmd.OptionableAppTemplate;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class ExecBaseGWEModel extends ExecutableModel {
	
	protected OptionTemplate clmOptionSlicer;
	protected OptionTemplate clmOptionVariables;

	protected PMString clmOptionCluster;
	protected PMString clmOptionPort;
	protected PMString clmOptionAccount;
	protected PMString clmOptionPassword;
	protected PMString clmOptionInDir;
	protected PMString clmOptionOutDir;
	
	protected GroupModel gridGroupModel;
	protected GroupModel inputGroupModel;
	protected GroupModel outputGroupModel;
	protected GroupModel algorithmGroupModel;
	
	public ExecBaseGWEModel(String title, String category, String description) {
		super(title, category, description);
		// Base Groups
		gridGroupModel = addGroup("GWE Settings", "Slicer3 Grid Settings");
		inputGroupModel = addGroup("Input File Settings", "Determine where the input files are located.");
		outputGroupModel = addGroup("Output File Settings", "Determines where the output files will be written");
		algorithmGroupModel = addGroup("Algorithm Parameters", "Parameters that determine how the algorithm runs");

		// Base Parameters
		clmOptionCluster   = gridGroupModel.addParam(new PMString("Cluster Head Node", "gweHost", "Address of the cluster head node where to reach GWE daemon", "cluster.bwh.harvard.edu"));
		clmOptionPort      = gridGroupModel.addParam(new PMString("Cluster Head Node Port", "gweHost", "Address of the cluster head node where to reach GWE daemon", "1099"));
		clmOptionAccount   = gridGroupModel.addParam(new PMString("Account", "gweUser", "Account to ssh into cluster head node"));
		clmOptionPassword  = gridGroupModel.addParam(new PMString("Password", "gwePassword", "Password of the account"));
		clmOptionSlicer    = gridGroupModel.addParam(new PMString("Slicer Location", "slicerLocation", "Location of Slicer Build directory on the cluster"));
		clmOptionVariables = gridGroupModel.addParam(new PMString("Iteration Variables", "iterationVariables", "Variables to iterate to generate the commands to run in the cluster"));
		
//		clmOptionInDir  = inputGroupModel.addParam(new PMDir("Input Directory", "inputDirectory", "This is the location of the data files on your local computer."));
//		clmOptionOutDir = inputGroupModel.addParam(new PMString("Remote Data Directory", "remoteInputDirectory", "This is the location of the data files on the compute cluster."));
	}
	
	public OptionableAppTemplate clone() {
		OptionableAppTemplate result = super.clone();
		result.removeOption(clmOptionSlicer);
//		result.removeOption(clmOptionInDir);
//		result.removeOption(clmOptionOutDir);
		return result;
	}
}

