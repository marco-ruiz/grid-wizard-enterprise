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

package org.gwe.drivers;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ResourceLink;

/**
 * @author Marco Ruiz
 * @since Dec 6, 2008
 */
public class ResourceHandleFactory extends EnumMap<ProtocolScheme, Class> {

	private static Log log = LogFactory.getLog(ResourceHandleFactory.class);

	public ResourceHandleFactory(Map<ProtocolScheme, Class> values) {
		this();
		this.putAll(values);
    }

	public ResourceHandleFactory() {
	    super(ProtocolScheme.class);
    }
	
	public <HANDLE_TYPE extends ResourceHandle> HANDLE_TYPE createHandle(ResourceLink<HANDLE_TYPE> link) throws HandleCreationException {
		try {
			return (HANDLE_TYPE) getHandleClass(link).getConstructor(ResourceLink.class).newInstance(link);
	    } catch (Exception e) {
	    	String msg = "Problems creating handle for resource '" + link.getURI() + "' using reflection";
	    	log.warn(msg, e);
			throw new HandleCreationException(msg, e);
	    }
	}

	public <HANDLE_TYPE extends ResourceHandle> Class<HANDLE_TYPE> getHandleClass(ResourceLink<HANDLE_TYPE> link) {
		return get(link.getURI().getProtocolScheme());
	}
}
