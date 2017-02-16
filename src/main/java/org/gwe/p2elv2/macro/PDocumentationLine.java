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

package org.gwe.p2elv2.macro;

import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since Dec 23, 2008
 */
@REXConfig4Class(rexPieces={"\\s*//\\s*", "tag", "content"})
public class PDocumentationLine {
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[^\\s^:]*", suffix=":"), optional=true)
	private String tag;
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[^\n]*"))
	private String content;
	
	public String getTag() {
    	return tag;
    }

	public void setTag(String id) {
    	this.tag = id;
    }

	public String getContent() {
    	return content;
    }

	public void setContent(String description) {
    	this.content = description;
    }
	
	public String toString() {
		return tag + "=" + content; 
	}
}
