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
public class HtmlContent {
	
	protected static final String NO_VAL = "---";

	private String caption;
	private HtmlLink link = null;
	private WebIcon image = null;
	
	public HtmlContent(Object caption, HtmlLink link) {
		this(caption, link, null);
	}
	
	public HtmlContent(Object caption, HtmlLink link, WebIcon image) {
	    this(caption);
	    this.link = link;
	    this.image = image;
    }
	
	public HtmlContent(Object caption) {
		this.caption = (caption == null) ? NO_VAL : caption.toString();
    }

	public HtmlLink getLink() {
    	return link;
    }

	public StringBuffer getHTML() {
	    StringBuffer displayedContent = (image == null) ? new StringBuffer(caption) : new HtmlImage(image, caption).getHTML();
		return (link != null) ? link.getHTML(displayedContent) : displayedContent;
    }
}

