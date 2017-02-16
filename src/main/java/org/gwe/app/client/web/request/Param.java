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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.JobInfo;
import org.gwe.persistence.model.OrderInfo;

/**
 * @author Marco Ruiz
 * @since Dec 14, 2008
 */
public class Param<VALUE_TYPE, OBJECT_TYPE> {

	private static final Set<Param> params = new HashSet<Param>();
	
	public static final Param<String,  HeadResourceInfo> CLUSTER_ID = new Param<String, HeadResourceInfo>  ("clusterId", "selectedCluster", new StringParser()); 
	public static final Param<Integer, OrderInfo>        ORDER_ID   = new Param<Integer, OrderInfo>        ("orderId",   "selectedOrder",   new IntegerParser()); 
	public static final Param<Integer, JobInfo>          JOB_NUM    = new Param<Integer, JobInfo>          ("jobNum",    "selectedJob",     new IntegerParser());
	public static final Param<Boolean, Object>           INSTALL    = new Param<Boolean, Object>           ("install",   "",                new BooleanParser());
	public static final Param<String,  OrderInfo>        P2EL       = new Param<String, OrderInfo>         ("p2el",      "queuedOrder",     new StringParser());
	public static final Param<String,  List<String>>     COMMANDS   = new Param<String, List<String>>      ("commands",  "commands",        new StringParser());
	public static final Param<String,  String>           OPER       = new Param<String, String>            ("operation", "operationResult", new StringParser());
	public static final Param<String,  String>           BUNDLE     = new Param<String, String>            ("bundle",    "",                new StringParser());

	public static Set<Param> values() {
		return new HashSet<Param>(params);
	}
	
	private String fieldId;
	private String objectIdentifiedName;
	private ParamParser<VALUE_TYPE> parser;
	
	protected Param(String fieldId, String objectIdentifiedName, ParamParser<VALUE_TYPE> paramParser) { 
		this.fieldId = fieldId;
		this.objectIdentifiedName = objectIdentifiedName;
		this.parser = paramParser;
		params.add(this);
	}
	
	public String getFieldId() { 
		return fieldId; 
	}
	
	public Object extractFrom(HttpServletRequest request) {
        return (request == null) ? null : request.getParameter(fieldId);
	}

	public VALUE_TYPE parse(Object value) {
    	return parser.parse(value);
    }

	public String getObjectIdentifiedName() {
    	return objectIdentifiedName;
    }
	
	public String toString() {
		return fieldId;
	}
}

interface ParamParser<VALUE_TYPE> {
	public VALUE_TYPE parse(Object value);
}

class StringParser implements ParamParser<String> {
	public String parse(Object value) {
		return (value != null) ? value.toString() : null;
    }
}

class IntegerParser implements ParamParser<Integer> {
	public Integer parse(Object value) {
		try {
			return Integer.parseInt(value.toString());
		} catch(Exception e) {
			return null;
		}
    }
}

class BooleanParser implements ParamParser<Boolean> {
	public Boolean parse(Object value) {
		try {
			return Boolean.parseBoolean(value.toString());
		} catch(Exception e) {
			return false;
		}
    }
}
