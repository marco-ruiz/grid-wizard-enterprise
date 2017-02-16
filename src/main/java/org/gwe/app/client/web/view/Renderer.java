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

import java.util.List;
import java.util.Map;

import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.explorer.VarsModelRenderer;
import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobExecutionInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderExecutionProfileInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.web.HtmlImage;
import org.gwe.utils.web.HtmlMapTable;
import org.gwe.utils.web.HtmlTable;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2008
 */
public class Renderer {
	
	// DETAILS TABLES
	
	public String renderDetails(HeadResourceInfo daemonInfo) {
		return renderTableModel(new HtmlClusterPropsTable(daemonInfo));
	}

	public String renderDetails(OrderInfo order) {
		return renderTableModel(new HtmlOrderPropsTable(order));
	}

	public String renderDetails(JobInfo job) {
		return renderTableModel(new HtmlJobPropsTable(job));
	}

	public String renderDetails(JobExecutionInfo exec) {
		return renderTableModel(new HtmlExecPropsTable(exec));
	}

	// ELEMENTS TABLES
	
	public String renderClusters(ClientConfig config) {
		return renderTableModel(new HtmlClustersTable(config));
	}

	public String renderKeys(ClientConfig config) {
		return renderTableModel(new HtmlKeysTable(config));
	}

	public String renderElements(HeadResourceInfo daemonInfo) {
		return renderTableModel(new HtmlOrdersTable(daemonInfo));
	}

	public String renderElements(String clusterId, OrderInfo order) {
		return renderTableModel(new HtmlJobsTable(clusterId, order));
	}

	public String renderElements(String clusterId, JobInfo job) {
		return renderTableModel(new HtmlExecsTable(clusterId, job));
	}
	
	// OTHER TABLES 
	
	public String renderExecutionProfile() {
		return renderTableModel(new HtmlOrderProfilePropsTable(new OrderExecutionProfileInfo()));
	}
	
	public String render(PPermutation perm) {
		return render((perm == null) ? null : perm.asFriendlyTreeMap());
	}
	
	public String render(Map<String, Object> data) {
		return renderTableModel(new HtmlMapTable(data));
	}
	
	public String render(List<PVariable> vars) {
		return renderTableModel(new HtmlPVarTable(vars));
	}
	
	public String renderTableModel(HtmlTable tableModel) {
		return tableModel.getHTML();
	}
	
	// OTHER WIDGETS 
	
	public String renderStatusImage(JobExecutionInfo exec) {
		return new HtmlImage(HtmlExecPropsTable.getStatusImage(exec), "Job Status").getHTML().toString();
	}
	
	public String renderResultsModel(OrderInfo order) {
		return new VarsModelRenderer().renderJS(order);
	}
	
	public String renderElements(List elements) {
		StringBuffer result = new StringBuffer();
		for (Object ele : elements)
	        result.append(ele).append("\n");
	        
		return result.toString();
	}
}

