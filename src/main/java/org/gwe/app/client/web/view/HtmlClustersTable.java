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

package org.gwe.app.client.web.view;

import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.app.client.SessionsRepository;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.web.request.Param;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.utils.web.HtmlLink;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;
import org.gwe.utils.web.WebIcon;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2008
 */
public class HtmlClustersTable extends HtmlTable {

	public static String getStatusIcon(Session4ClientAPIEnhancer session) {
		return (session == null) ? "icon_error_sml.gif" : "icon_success_sml.gif";
	}
	
	public HtmlClustersTable(ClientConfig config) {
	    super("Status", "Name", "Host", "Queue Size", "Wait Connection Timeout", "Hijack Timeout", "Idle Timeout", "Variables");
	    SessionsRepository sessionsRepo = config.getSessionsRepository();
    	for (HeadResourceInfo obj : config.getGrid().getHeadResources()) {
    		Session4ClientAPIEnhancer session = null;
			try {
	            session = sessionsRepo.getSession(obj, false);
            } catch (Exception e) {}
    	    WebIcon statusImage = WebIcon.getImageFor(session);
			String tootip = (statusImage == WebIcon.STATUS_OK) ?  "GWE enabled cluster" : "Cluster not enabled with GWE";
			HtmlTableCell status = new HtmlTableCell("", tootip, null, statusImage);
    	    HtmlTableCell name = new HtmlTableCell(obj.getName(), "", new HtmlClusterLink(obj.getName()));
        	addRow(status, name, obj.getHost(), obj.getQueueSize(), getMinutes(obj.getMaxWaitMins()), getMinutes(obj.getMaxHijackMins()), getMinutes(obj.getMaxIdleMins()), obj.getVarsAsMap());
    	}
    }
	
	private String getMinutes(float minutes) {
		return minutes + " minutes";
	}
}

class HtmlClusterLink extends HtmlLink {

	public HtmlClusterLink(String clusterId) {
	    super(false, "cluster");
	    addParam(Param.CLUSTER_ID, clusterId);
    }
}
