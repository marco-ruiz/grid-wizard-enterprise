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

import java.util.HashSet;
import java.util.Set;

import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2008
 */
public class ConfigParam<VALUE_TYPE, OBJECT_TYPE> extends Param<VALUE_TYPE, OBJECT_TYPE> {

	private static final Set<ConfigParam> params = new HashSet<ConfigParam>();
	
	public static final ConfigParam<String,  HeadResourceInfo> CLUSTER_ID = new ConfigParam<String, HeadResourceInfo>  ("clusterId", "selectedCluster", new StringParser()); 
	public static final ConfigParam<Integer, OrderInfo>        ORDER_ID   = new ConfigParam<Integer, OrderInfo>        ("orderId",   "selectedOrder",   new IntegerParser()); 
	public static final ConfigParam<Integer, JobInfo>          JOB_NUM    = new ConfigParam<Integer, JobInfo>          ("jobNum",    "selectedJob",     new IntegerParser());
	public static final ConfigParam<Boolean, Object>           INSTALL    = new ConfigParam<Boolean, Object>           ("install",   "",                new BooleanParser());
	public static final ConfigParam<String,  OrderInfo>        P2EL       = new ConfigParam<String, OrderInfo>         ("p2el",      "queuedOrder",     new StringParser());
	public static final ConfigParam<String,  String>           OPER       = new ConfigParam<String, String>            ("operation", "operationResult", new StringParser());

	public static Set<Param> values() {
		return new HashSet<Param>(ConfigParam.params);
	}
	
	protected ConfigParam(String fieldId, String objectIdentifiedName, ParamParser<VALUE_TYPE> paramParser) { 
		super(fieldId, objectIdentifiedName, paramParser);
		params.add(this);
	}
}
