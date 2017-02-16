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

package org.gwe.utils.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.gwe.app.client.web.request.PageModel;
import org.gwe.app.client.web.request.Param;

/**
 * @author Marco Ruiz
 * @since Dec 15, 2008
 */
public class HtmlLink extends HashMap<Object, Object> {
	
	private String context;
	private boolean external = false;
	
	public HtmlLink(boolean external, String context, PageModel model) {
	    this(external, context);
	    for (Param param : Param.values())
	        put(param, model.getParam(param));
    }
	
	public HtmlLink(boolean external, String context) {
		this.external = external;
	    this.context = context;
    }
	
	public HtmlLink addParam(Object key, Object value) {
		put(key, value);
		return this;
	}
	
	public String render() {
		StringBuffer result = new StringBuffer();
		if (context.contains("#")) return context;
		result.append(context + "?");
		for (Map.Entry<Object, Object> entry : entrySet())
			result.append(encode(entry.getKey()) + "=" + encode(entry.getValue()) + "&");
		
		return result.toString(); 
	}
	
	public StringBuffer getHTML(StringBuffer content) {
		StringBuffer result = new StringBuffer();
	    result.append("<a href=\"").append(render()).append("\" ").append(getAttributes()).append(">");
		result.append(content);
		result.append("</a>");
		return result;
    }

	private String encode(Object value) {
		String valueStr = (value == null) ? "" : value.toString();
		try {
	        return URLEncoder.encode(valueStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        	return valueStr;
        }
    }

	public String getAttributes() {
	    return external ? " class=\"externalLink\" " : "";
    }
}
