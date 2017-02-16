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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marco Ruiz
 * @since May 28, 2008
 */
public class ExecutableModelParser {

	public ExecutableModel readExecutableModel(String xml) throws Exception {
		ByteArrayInputStream xmlStream = new ByteArrayInputStream(xml.getBytes());
		Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
		Element root = (Element)dom.getDocumentElement().getChildNodes();
		ExecutableModel result = new ExecutableModel(
					readVal(root, "title"),
					readVal(root, "category"),
					readVal(root, "description"));
		
		result.setVersion(readVal(root, "version"));
		result.setUrl(readVal(root, "documentation-url"));
		result.setLicense(readVal(root, "license"));
		result.setContributor(readVal(root, "contributor"));
		result.setAcknowledgements(readVal(root, "acknowledgements"));

		NodeList children = root.getElementsByTagName("parameters");
		if (children != null)
			for (int idx = 0; idx < children.getLength(); idx++) {
				Element groupEle = (Element)children.item(idx);
				String label = readVal(groupEle, "label");
				String description = readVal(groupEle, "description");
				readGroup(groupEle, result.addGroup(label, description));
			}
		
		return result;
	}

	private void readGroup(Element groupEle, GroupModel groupModel) {
	    NodeList children = groupEle.getChildNodes();
	    if (children != null)
	    	for (int idx = 0; idx < children.getLength(); idx++) {
	    		Node child = children.item(idx);
	    		if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element paramEle = (Element)child;
		    		String valueType = paramEle.getTagName();
		    		if (!valueType.equals("label") && !valueType.equals("description")) 
		    			groupModel.addParam(readParameter(paramEle));
	    		}
	    	}
    }

	private ParameterModel readParameter(Element paramEle) {
		String valueType = paramEle.getTagName();

		String name = extractVal(paramEle, "name");
		String label = extractVal(paramEle, "label");
		String flag = extractVal(paramEle, "flag");
		String longFlag = extractVal(paramEle, "longflag");
		String description = extractVal(paramEle, "description");
		String defaultValue = extractVal(paramEle, "default");
		String index = extractVal(paramEle, "index");
		String channel = extractVal(paramEle, "channel");
		
		ParameterModel result = new ParameterModel(valueType, label, longFlag, description, defaultValue);
		result.setName(name);
		result.setFlag(flag);
		if (index != null) result.setIndex(Integer.parseInt(index));
		result.setChannel(channel);
		result.setFiller(generateChildrenXMLFragment(paramEle));
		return result;
    }

	private String extractVal(Element ele, String tagName) {
		return readVal(ele, tagName, true);
	}
	
	private String readVal(Element ele, String tagName, boolean... extract) {
		String result = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			Node firstChild = el.getFirstChild();
			if (firstChild != null) {
				result = firstChild.getNodeValue();
				if (extract.length > 0 && extract[0]) ele.removeChild(el);
			}
		}
		return result;
	}
	
	public String generateXMLFragment(Element node) {
		String nodeName = node.getNodeName();
		String nodeValue = getElementValue(node);
		NamedNodeMap attributes = node.getAttributes();

		String result = "<" + nodeName;
		for (int index = 0; index < attributes.getLength(); index++) {
			Node attr = attributes.item(index);
			result += " " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\" ";
		}
		
		return result + ">" + nodeValue + generateChildrenXMLFragment(node) + "</" + nodeName + ">"; 
	}

	private String generateChildrenXMLFragment(Element node) {
		String result = "";
	    NodeList children = node.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (child.getNodeType() == Node.ELEMENT_NODE)
				result += generateXMLFragment((Element)child);
		}
	    return result;
    }
	
    public final static String getElementValue(Node elem) {
    	Node kid;
    	if( elem != null){
        	if (elem.hasChildNodes()){
            	for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
                	if( kid.getNodeType() == Node.TEXT_NODE  ){
                		return kid.getNodeValue();
                	}
                }
            }
    	}
    	return "";
    }
}

