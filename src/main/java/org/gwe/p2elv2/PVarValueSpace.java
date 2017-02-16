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

package org.gwe.p2elv2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Aug 18, 2008
 */
public class PVarValueSpace extends ArrayList<PVarValue> {

	public PVarValueSpace(List<String> params) {
		this(params.toArray(new String[] {}));
	}
	
	public PVarValueSpace(String... params) {
		if (params != null)
			for (int idx = 0; idx < params.length; idx++) 
				add(params[idx]);
	}
	
	public void add(String param) {
		add(new PVarValue<String>(param));
    }
}
