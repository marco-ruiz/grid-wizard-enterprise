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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gwe.persistence.model.BaseModelInfo;
import org.gwe.persistence.model.EventType;
import org.gwe.persistence.model.ModelSummary;

/**
 * @author Marco Ruiz
 * @since Oct 5, 2007
 */
public class Event implements Serializable {
//	private BaseModelInfo<?> source;
	private EventType eventType;
	private List<ModelSummary> modelSums = new ArrayList<ModelSummary>();
	
	public Event(EventType evType, BaseModelInfo<?> source, BaseModelInfo... models) {
		modelSums.add(source.createModelSummaryFor(evType));
		for (BaseModelInfo modelInfo : models) modelSums.add(modelInfo.createModelSummaryFor(evType));
		eventType = evType;
	}

	public EventType getEventType() {
		return eventType;
	}

	public List<ModelSummary> getModelIdentifiers() {
		return modelSums;
	}

	public String toString() {
		String result = "EVENT [" + eventType + "] =====> ";

		for (int idx = 0; idx < modelSums.size(); idx++) {
			ModelSummary modelSum = modelSums.get(idx);
			result += 	"{" + createFieldEntry(idx, "SRC", modelSum.getModelInfoClass().getSimpleName()) +
						"," + createFieldEntry(idx, "KEY", modelSum.getKey()) +
						"," + createFieldEntry(idx, "PAYLOAD", modelSum.getPayload()) +
						" }";
		}
		return result;
	}

	private String createFieldEntry(int idx, String fieldName, Object value) {
		if (value == null || value.toString().equals("")) return "";
		return " M(" + idx + ")." + fieldName + "=[" + value + "]";
	}
	
	
	
}
