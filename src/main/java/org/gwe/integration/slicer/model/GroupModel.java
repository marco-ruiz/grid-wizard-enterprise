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

package org.gwe.integration.slicer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class GroupModel {
	private String label = "";
	private String description = "";
	private boolean advanced = false;
	private List<ParameterModel> parameters = new ArrayList<ParameterModel>();
	
	public GroupModel(String label, String description) {
		this.label = label;
		this.description = description;
	}

	public GroupModel() {
	}

	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isAdvanced() {
		return advanced;
	}
	
	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
	}

	public List<ParameterModel> getParameters() {
		return parameters;
	}
	
	public <PM_TYPE extends ParameterModel> PM_TYPE addParam(PM_TYPE param) {
		this.parameters.add(param);
		return param;
	}
}

