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

package org.gwe.p2elv2.functions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;
import org.gwe.p2elv2.PVarValueSpace;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * @author Marco Ruiz
 * @since Aug 11, 2008
 */
public class PFXmlEdit extends PFunction {

	private static Log log = LogFactory.getLog(PFXmlEdit.class);

	public PFXmlEdit() { super("xpath"); }

    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
        PVarValueSpace result = new PVarValueSpace();
        String source = params.get(0);
        String expression = params.get(1);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			InputSource inputSource = new InputSource(readSource(source, ctx));
	        NodeList queryResult = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
	        int length = queryResult.getLength();
			for (int idx = 0; idx < length; idx++) {
		        Node item = queryResult.item(idx);
				String value = (item.getNodeType() == Node.ATTRIBUTE_NODE || expression.endsWith("text()")) ? 
						item.getTextContent() : createXMLString(item);
				value = value.replaceAll("^\\s*", "").replaceAll("\\s*$", "");
				log.info("XPath value " + idx + " formatted = [" + value + "]");
				result.add(new PVarValue(value));
            }
			if (result.size() == 0) result.add(new PVarValue(""));
        } catch (Exception e) {
        	e.printStackTrace();
        	log.warn("Error while parsing source '" + source + "' using xpath expression '" + expression + "'", e);
        }
		
    	return result;
    }
    
    private InputStream readSource(String source, PStatementContext ctx) {
    	try {
	        return ctx.getKeys().createFileLink(source).createHandle().getInputStream();
        } catch (Exception e) {
        	return new ByteArrayInputStream(source.getBytes());
        }
    }
    
    private String createXMLString(Node source) throws TransformerFactoryConfigurationError, TransformerException {
    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
    	transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    	StreamResult result = new StreamResult(new StringWriter());
    	transformer.transform(new DOMSource(source), result);

    	return result.getWriter().toString();
    }

	private HashMap<String, String> readAttributes(Node ele) {
		HashMap<String, String> result = new HashMap<String, String>();
	    NamedNodeMap attrs = ele.getAttributes();
	    for (int idx = 0; idx < attrs.getLength(); idx++) {
	    	Node attr = attrs.item(idx);
	    	result.put(attr.getNodeName(), attr.getNodeValue());
	    }
	    return result;
    }
    
    public static void main(String[] args) {

    	List<String> params = new ArrayList<String>();
    	String xml = "<catalog><entries>" +
    			"<entry URI=\"uri2\" desc=\"desc2\">content2</entry>" +
    			"<entry URI=\"uri3\" desc=\"desc3\">content3</entry>" +
    			"<entry URI=\"uri4\" desc=\"desc4\">content4</entry>" +
    			"</entries></catalog>";
    	
    	xml = "<ResultSet title=\"Scans\"><results><columns><column>ID</column><column>Name</column><column>Size</column><column serverRoot=\"\">URI</column></columns>" + 
    	"<rows>" + 
    	"<row><cell>1394/OAS1_0101_MR1_mpr-3_anon.hdr</cell><cell>OAS1_0101_MR1_mpr-3_anon.hdr</cell><cell>348</cell><cell>/REST/projects/CENTRAL_OASIS_CS/subjects/OAS1_0101/experiments/OAS1_0101_MR1/scans/mpr-3/resources/1394/files/OAS1_0101_MR1_mpr-3_anon.hdr</cell></row>" + 
    	"<row><cell>1394/OAS1_0101_MR1_mpr-3_anon.img</cell><cell>OAS1_0101_MR1_mpr-3_anon.img</cell><cell>16777216</cell><cell>/REST/projects/CENTRAL_OASIS_CS/subjects/OAS1_0101/experiments/OAS1_0101_MR1/scans/mpr-3/resources/1394/files/OAS1_0101_MR1_mpr-3_anon.img</cell></row>" + 
    	"<row><cell>1394/OAS1_0101_MR1_mpr-3_anon_sag_66.gif</cell><cell>OAS1_0101_MR1_mpr-3_anon_sag_66.gif</cell><cell>35836</cell><cell>/REST/projects/CENTRAL_OASIS_CS/subjects/OAS1_0101/experiments/OAS1_0101_MR1/scans/mpr-3/resources/1394/files/OAS1_0101_MR1_mpr-3_anon_sag_66.gif</cell></row>" + 
    	"</rows></results></ResultSet>";
  
    	params.add(xml);
//    	params.add("//rows/row/cell[4]/text()");
    	params.add("//rows/row/cell[(position()=4)and((substring-after(.,'.')='hdr')or(substring-after(.,'.')='nrrd'))]/text()"); // [ends-with(., '.hdr')]");
//    	params.add("//rows/row/cell[position()=1]"); // [ends-with(., '.hdr')]");
//    	params.add("//cell[starts-with(., '.hdr')]");
    	
    	
    	PFXmlEdit function = new PFXmlEdit();
		PVarValueSpace result = function.calculateValues(params, null);
		System.out.println(result);
    }
    
    public static void main2(String[] args) {
    	System.out.println("\n\r\nALFA\n\r\n".replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
    	
    	List<String> params = new ArrayList<String>();
    	params.add("<catalog><entries><entry URI=\"uri2\">content2</entry><entry URI=\"uri3\">content3</entry><entry URI=\"uri4\">content4</entry></entries></catalog>");
    	params.add("//rows/row/cell[4]/text()");
    	
    	PFXmlEdit function = new PFXmlEdit();
		PVarValueSpace result = function.calculateValues(params, null);
		System.out.println(result);
		
		for (PVarValue varValue : result) {
			params.clear();
			params.add(varValue.toString());
			params.add("/entry/@URI");
			
			PVarValueSpace entryRes = function.calculateValues(params, null);
			System.out.println(entryRes);
        }
    }
    
}
