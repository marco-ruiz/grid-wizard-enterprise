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

package org.gwe.utils;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Marco Ruiz
 * @since May 28, 2008
 */
public class BaseXMLParser {

	public static <RET_TYPE> RET_TYPE processNodeList(NodeList nl, XMLProcessor<RET_TYPE> processor, RET_TYPE result){
		if (nl != null && nl.getLength() > 0)
			for (int index = 0; index < nl.getLength(); index++) 
				processor.processNode(result);
		
		return result;
	}

	protected Document dom;

	public BaseXMLParser(String filename) throws SAXException, IOException, ParserConfigurationException {
		dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filename);
	}

	protected String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	protected int getIntValue(Element ele, String tagName) {
		return Integer.parseInt(getTextValue(ele,tagName));
	}
	
	protected interface XMLProcessor<RET_TYPE> {
		public RET_TYPE processNode(RET_TYPE arg);
	}
}
