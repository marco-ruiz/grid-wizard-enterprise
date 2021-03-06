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

package org.gwe.api.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gwe.persistence.model.BaseModelInfo;
import org.gwe.persistence.model.EventType;

/**
 * @author Marco Ruiz
 * @since Oct 5, 2007
 */
public class DefaultEventFilter implements EventFilter {
	
	private List<EventType> evTypes;
	private List<BaseModelInfo> sources;
	
	public DefaultEventFilter() {
		this(null);
	}

	public DefaultEventFilter(EventType... evTypes) {
		this.evTypes = (evTypes != null) ? Arrays.asList(evTypes) : new ArrayList<EventType>();
	}

	public List<EventType> getEventTypes() {
		return evTypes;
	}
	
	public void setSources(BaseModelInfo... sources) {
		this.sources = Arrays.asList(sources);
	}

	public List<BaseModelInfo> getSources() {
		return sources;
	}
}
