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

package org.gwe.persistence.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ModelSummary<KEY_TYPE> implements Serializable {

	private Class<? extends BaseModelInfo> modelInfoClass;
	private KEY_TYPE key;
	private List<Serializable> payload;

	public ModelSummary(BaseModelInfo<KEY_TYPE> baseModelInfo, Serializable... payload) {
		modelInfoClass = baseModelInfo.getClass();
		key = baseModelInfo.getId();
		this.payload = Arrays.asList(payload);
	}

	public Object getKey() {
		return key;
	}

	public Class<? extends BaseModelInfo> getModelInfoClass() {
		return modelInfoClass;
	}

	public Object getPayload() {
		return payload;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((modelInfoClass == null) ? 0 : modelInfoClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		final ModelSummary<KEY_TYPE> other = (ModelSummary<KEY_TYPE>) obj;

		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		
		if (modelInfoClass == null) {
			if (other.modelInfoClass != null)
				return false;
		} else if (!modelInfoClass.equals(other.modelInfoClass))
			return false;
		
		return true;
	}
}