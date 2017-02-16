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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.web.view.Renderer;

/**
 * @author Marco Ruiz
 * @since Dec 15, 2008
 */
public class PageModel extends HashMap<String, Object> {

	public PageModel(HttpServletRequest request, ClientConfig config) {
		for (Param field : Param.values())
			setParam(field, field.extractFrom(request));
		
        put("request", request);
        put("config", config);
        put("version", config.getAppContext().getDistribution().getVersion());
        put("renderer", new Renderer());
    }

	public Object setParam(Param field, Object value) {
	    return put(field.getFieldId(), value);
    }

	public <VALUE_TYPE> VALUE_TYPE getParam(Param<VALUE_TYPE, ?> field) {
		Object value = get(field.getFieldId());
		VALUE_TYPE result = field.parse(value);
		return "".equals(result) ? null : result;
	}

	public Operation getOperation() {
		return Operation.getOperation(getParam(Param.OPER));
	}

	public <VALUE_TYPE, OBJECT_TYPE> void addIdentifiedObject(Param<VALUE_TYPE, OBJECT_TYPE> field, OBJECT_TYPE object) {
		put(field.getObjectIdentifiedName(), object);
	}

	public <VALUE_TYPE, OBJECT_TYPE> OBJECT_TYPE getIdentifiedObject(Param<VALUE_TYPE, OBJECT_TYPE> field) {
		return (OBJECT_TYPE) get(field.getObjectIdentifiedName());
	}
}
