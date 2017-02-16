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

import org.gwe.app.client.web.request.Operation;
import org.gwe.app.client.web.request.Param;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.web.HtmlLink;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;

/**
 * @author Marco Ruiz
 * @since Dec 18, 2008
 */
public class HtmlOrdersTable extends HtmlTable {

	public HtmlOrdersTable(HeadResourceInfo daemonInfo) {
	    super("Id", "", "Submitted at", "Completed at", "Progress (completed / total)", "");
		if (daemonInfo == null) return;
        for (OrderInfo order : daemonInfo.getOrdersList()) {
        	String clusterId = daemonInfo.getName();
			HtmlTableCell id = new HtmlTableCell("[ " + order.getId() + " ]", "", new HtmlOrderLink(clusterId, order)) ;
			HtmlTableCell pause = getOperationLink(clusterId, order, order.isPaused() ? Operation.RESUME : Operation.PAUSE);
        	String progress = order.getCompletedJobsCount() + " / " + order.getTotalJobsCount();
//        	HtmlContent priority = getOperationLink(clusterId, order, Operation.DOWN);
//        	priority.getLink().addParam(Param.OTHER_ORDER_ID, prevOrderId);
        	HtmlTableCell delete = getOperationLink(clusterId, order, Operation.DELETE);

			addRow(id, pause, order.getWhenCreated(), order.getWhenCompleted(), progress, delete);
        }
	    return;
    }

	private HtmlTableCell getOperationLink(String clusterId, OrderInfo order, Operation oper) {
		return new HtmlTableCell(order.getId() + "", "", new HtmlOperLink(clusterId, order, oper), oper.getImage());
	}
}

class HtmlOrderLink extends HtmlLink {

	public HtmlOrderLink(String clusterId, OrderInfo order) {
	    super(false, "order");
	    addParam(Param.CLUSTER_ID, clusterId);
	    addParam(Param.ORDER_ID, order.getId());
    }
}

class HtmlOperLink extends HtmlLink {

	public HtmlOperLink(String clusterId, OrderInfo order, Operation oper) {
	    super(false, "cluster");
	    addParam(Param.CLUSTER_ID, clusterId);
	    addParam(Param.ORDER_ID, order.getId());
	    addParam(Param.OPER, oper.toString());
    }
}
