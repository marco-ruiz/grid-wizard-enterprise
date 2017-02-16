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

package org.gwe.app.client.web.view;

import java.io.IOException;
import java.io.InputStream;

import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Dec 12, 2008
 */
public class MainVelocityTemplate {

	private static final String CONTENT_BOX = "${meta_contentBox}";
	
    public static String readTemplateFile(String templateName) throws IOException {
	    templateName = MainVelocityTemplate.class.getPackage().getName().replace('.','/') + "/" + templateName;
		InputStream is = MainVelocityTemplate.class.getClassLoader().getResourceAsStream(templateName);
		return new String(IOUtils.readStream(is, null).toByteArray());
    }
    
	private String mainPage = "";
    private String contentMacros = "";
    
    private String layoutWithMacros = "";
	
    public MainVelocityTemplate() throws IOException {
    	this("main.html", "macros.vm");
    }
    
    public MainVelocityTemplate(String mainTemplate, String contentMacros) throws IOException {
    	this.mainPage      = readTemplateFile(mainTemplate);
    	this.contentMacros = readTemplateFile(contentMacros);
    	this.layoutWithMacros = this.contentMacros + "\n" + mainPage;
    }
    
	public String createPageTemplate(String templateFileName) throws IOException {
	    String template = readTemplateFile(templateFileName);
//	    String parsedContent = VelocityUtils.evaluate(contentMacros + "\n" + template);
		return layoutWithMacros.replace(CONTENT_BOX, template);
    }
	
	public String getMainPage() {
    	return mainPage;
    }

	public static void main(String[] args) throws IOException {
		String page = new MainVelocityTemplate().createPageTemplate("grid.html");
		System.out.println(page);
	}
}
