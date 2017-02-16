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


/**
 * @author Marco Ruiz
 * @since Dec 12, 2008
 */
public class HtmlTableCell {
	
	private CellAlignment alignment = CellAlignment.LEFT;
	private String tooltip = "";
	private boolean header = false;
	private HtmlContent[] content;
	
	public HtmlTableCell(String caption, String tooltip, HtmlLink link) {
		this(caption, tooltip, link, null);
	}
	
	public HtmlTableCell(String caption, String tooltip, HtmlLink link, WebIcon image) {
	    this(tooltip, new HtmlContent(caption, link, image));
	    if (link != null || image != null) this.alignment = CellAlignment.CENTER;
    }
	
	public HtmlTableCell(String caption, String tooltip) {
		this(tooltip, new HtmlContent(caption));
		this.alignment = CellAlignment.LEFT;
    }
	
	public HtmlTableCell(String tooltip, HtmlContent... content) {
		this.tooltip = tooltip;
		this.content = content;
		this.alignment = CellAlignment.CENTER;
    }
	
	public void setHeader(boolean value) {
    	this.header = value;
    }

	public void setAlignment(CellAlignment alignment) {
    	this.alignment = alignment;
    }

	public StringBuffer getHTML() {
		String htmlTag = header ? "th" : "td";
	    StringBuffer result = new StringBuffer();
	    
	    // Start cell tag
	    result.append("\t\t<").append(htmlTag).append(" align=\"").append(alignment).append("\"");
	    if (tooltip != null && !tooltip.equals("")) 
	    	result.append(" title=\"").append(tooltip).append("\" ");
	    result.append(">");

	    // cell content
	    result.append(getContent());
	    
	    // cell tag closure
		return result.append("</").append(htmlTag).append(">\n");
	}

	private String getContent() {
		if (content == null || content.length == 0) return "";
		StringBuffer result = new StringBuffer();
		for (HtmlContent cont : content) result.append(cont.getHTML());
	    return result.toString();
    }
}
