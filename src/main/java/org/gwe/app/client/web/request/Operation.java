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

package org.gwe.app.client.web.request;

import org.gwe.utils.web.WebIcon;

/**
 * @author Marco Ruiz
 * @since Jan 31, 2009
 */
public enum Operation {

	// Status
	PREVIEW("preview", WebIcon.OPER_PREVIEW),
	QUEUE(  "queue",   WebIcon.OPER_QUEUE),
	
	UP(     "swap",    WebIcon.OPER_UP),
	DOWN(   "swap",    WebIcon.OPER_DOWN),

	PAUSE(  "pause",   WebIcon.OPER_PAUSE),
	RESUME( "resume",  WebIcon.OPER_RESUME),
	DELETE( "delete",  WebIcon.OPER_DELETE),
	
	CLEANUP("cleanup", WebIcon.OPER_DELETE);
	
	public static Operation getOperation(String id) {
		if (id != null) {
			id = id.toLowerCase();
			for (Operation	oper : Operation.values())
		        if (oper.toString().equals(id)) return oper;
		}
		
		return null;
	}
	
	private String name;
	private WebIcon image;
	
	Operation(String fileName, WebIcon image) { 
		this.name = fileName; 
		this.image = image;
	}
	
	public WebIcon getImage() {
    	return image;
    }

	public String toString() {
		return name;
	}
}
