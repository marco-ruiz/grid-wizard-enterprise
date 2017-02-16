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

package org.gwe.integration.slicer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.integration.slicer.model.ExecutableModel;
import org.gwe.utils.IOUtils;
import org.gwe.utils.cmd.OptionableAppTemplate;

/**
 * @author Marco Ruiz
 * @since Dec 16, 2007
 */
public class CLMApp {

	private static Log log = LogFactory.getLog(CLMApp.class);
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ExecutableModel template = createExecutableModel(args);
		OptionableAppTemplate invocationTemplate = template.clone();
		invocationTemplate.getInvocationArgs();
	}

	private static ExecutableModel createExecutableModel(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));
		String templateClassName = argsList.remove(0);
		ExecutableModel template = (ExecutableModel) Class.forName(templateClassName).newInstance();
		template.setXMLTemplate(IOUtils.readClassPathFile("standardExecutionModel.vm"));
		template.loadArgs(argsList.toArray(new String[]{}), true);
		return template;
	}
}

