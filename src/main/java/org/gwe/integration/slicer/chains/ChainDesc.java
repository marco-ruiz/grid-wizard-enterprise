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

package org.gwe.integration.slicer.chains;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Marco Ruiz
 * @since Mar 31, 2008
 */
public class ChainDesc {
	
	private List<ChainCLMDesc> modules = new ArrayList<ChainCLMDesc>();

	public ChainDesc(String chainDescriptorFile) throws SAXException, IOException, ParserConfigurationException {
		Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(chainDescriptorFile);
		NodeList nl = dom.getDocumentElement().getElementsByTagName("module");
		if (nl != null)
			for (int index = 0; index < nl.getLength(); index++) 
				modules.add(readModule((Element)nl.item(index)));
	}

	private ChainCLMDesc readModule(Element moduleEle) {
		String id = moduleEle.getAttribute("id");
		Map<String, String> pipedArgs = new HashMap<String, String>();
		NodeList nl = moduleEle.getElementsByTagName("argument");
		if (nl != null)
			for (int index = 0; index < nl.getLength(); index++) {
				Element el = (Element)nl.item(index);
				pipedArgs.put(el.getAttribute("name"), el.getAttribute("value-ref").replace('.', '_'));
			}

		return new ChainCLMDesc(id, pipedArgs);
	}

	public List<ChainCLMDesc> getCLMs() {
	    return modules;
    }
	
	public String toString() {
		return modules.toString();
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		System.out.println(new ChainDesc("/Users/admin/work/eclipse-ws/gwe-core/tmp/chain.xml"));
	}
}
