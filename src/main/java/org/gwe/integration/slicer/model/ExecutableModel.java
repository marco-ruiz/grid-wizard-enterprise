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

import org.gwe.utils.IOUtils;
import org.gwe.utils.VelocityUtils;
import org.gwe.utils.cmd.OptionTemplate;
import org.gwe.utils.cmd.OptionableAppTemplate;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2007
 */
public class ExecutableModel extends OptionableAppTemplate {

	public static String getXMLHeader() {
		return "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
	}
	
	public final OptionTemplate OPT_XML = new OptionTemplate("xml", "XML. Displays the CLM schema.");
	public final OptionTemplate OPT_LOGO = new OptionTemplate("logo", "Logo. Returns a stream with a custom logo image for this CLM.");
	
	private String category;
	private String title;
	private String description;
	private String version = "1.0-Alpha";
	private String url = "";
	private String license = "http://www.apache.org/licenses/LICENSE-2.0.txt";
	private String contributor = "Jeff Grethe, Marco Ruiz";
	private String acknowledgements = "BIRN CC - 'Grid Wizard Enterprise' (GWE) Project";
	private List<GroupModel> groups = new ArrayList<GroupModel>();

	private String xmlTemplate;

	public ExecutableModel(String title, String category, String description) {
		this(title, category, description, "standardExecutionModel.vm");
	}
	
	public ExecutableModel(String title, String category, String description, String templateFilename) {
		this.category = category;
		this.title = title;
		this.description = description;
		setXMLTemplate(IOUtils.readClassPathFile(templateFilename));
	}
	
	public void setXMLTemplate(String xmlTemplate) {
		this.xmlTemplate = xmlTemplate;
	}

	public void loadArgs(String[] args) {
		loadArgs(args, false);
	}
	
	public void loadArgs(String[] args, boolean processXML) {
    	// Register the specific application options
    	for (GroupModel group : groups)
			for (OptionTemplate param : group.getParameters()) addArg(param);

    	// Register the system XML option
    	addArg(OPT_XML);
    	addArg(OPT_LOGO);
    	
    	// Invoke the super implementation of this method
    	super.loadArgs(args);

    	if (processXML) outputXMLIfRequested();
    }

	private void outputXMLIfRequested() {
		// Print XML representation and leave if XML option selected
    	if (isXMLSelected()) {
    		System.out.println(getXML());
    		System.exit(0);
    	}
	}

	public boolean isXMLSelected() {
		return getArg(OPT_XML) != null;
	}
	
	public boolean isLogoSelected() {
		return getArg(OPT_LOGO) != null;
	}
	
	public String getXML() {
		return getXMLHeader() + VelocityUtils.merge("executable", this, xmlTemplate);
    }
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String documentationURL) {
		this.url = documentationURL;
	}
	
	public String getContributor() {
		return contributor;
	}
	
	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getAcknowledgements() {
		return acknowledgements;
	}

	public void setAcknowledgements(String acknowledgements) {
		this.acknowledgements = acknowledgements;
	}

	public List<GroupModel> getGroups() {
		return groups;
	}
	
	public GroupModel addGroup(String label, String description) {
		GroupModel group = new GroupModel(label, description);
		this.groups.add(group);
		return group;
	}
	
	public List<ParameterModel> getParameters() {
		List<ParameterModel> result = new ArrayList<ParameterModel>();
		for (GroupModel group : groups) result.addAll(group.getParameters());
		return result;
	}
}
