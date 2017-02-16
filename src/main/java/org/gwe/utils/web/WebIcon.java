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
 * @since Dec 21, 2008
 */
public enum WebIcon {

	// Status
	STATUS_OK(   "ok", "icon_success_sml.gif"),
	STATUS_WARN( "warn", "icon_warning_sml.gif"),
	STATUS_INFO( "info", "icon_info_sml.gif"),
	STATUS_HELP( "help", "icon_help_sml.gif"),
	STATUS_ERROR("error", "icon_error_sml.gif"),
	
	// Operations 
	OPER_PREVIEW("preview", "report_magnify.png"),
	OPER_QUEUE(  "queue", "report_go.png"),

	OPER_UP(     "up", "arrow_up.png"),
	OPER_DOWN(   "down", "arrow_down.png"),
	
	OPER_PAUSE(  "pause", "pause.gif"),
	OPER_RESUME( "resume", "resume_co.gif"),
	OPER_DELETE( "delete", "trash.gif"),
	
	// Entities
	ENT_GRID("grid", ""),
	ENT_CLUSTER("cluster", "server.png"),
	ENT_ORDER("order", "report.png"),
	ENT_JOB("job", "page.png"),
	ENT_EXEC("execution", ""),
	ENT_PERM("permutation", ""),
	ENT_STMT("statement", "");
	
	public static WebIcon getImageById(String id) {
		if (id != null) {
			id = id.toLowerCase();
			for (WebIcon icon : WebIcon.values())
		        if (icon.toString().equals(id)) 
		        	return icon;
		}
		
		return null;
	}
	
	public static WebIcon getImageFor(Object value) {
		if (value == null) return STATUS_ERROR;
		if (value instanceof Boolean)
			return ((Boolean)value) ? STATUS_OK : STATUS_ERROR;
		return STATUS_OK;
	}
	
	private String id;
	private String fileName;
	
	WebIcon(String id, String fileName) {
		this.id = id;
		this.fileName = "img/" + fileName; 
	}
	
	public String toString() {
		return fileName;
	}
}

