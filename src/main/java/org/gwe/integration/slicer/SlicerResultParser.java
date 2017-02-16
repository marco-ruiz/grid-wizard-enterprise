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

package org.gwe.integration.slicer;

import java.io.Serializable;

import org.gwe.persistence.model.ResultParser;

/**
 * @author Marco Ruiz
 * @since Jan 28, 2008
 */
public class SlicerResultParser extends ResultParser {

	public Serializable parseResult(Serializable rawResult) {
		if (!(rawResult instanceof String)) return rawResult;
		String result = (String)rawResult;
		int idx = getNextCharIndex(result, "</filter-end>");
		return result.substring(idx);
	}

	private int getNextCharIndex(String result, String token) {
		return result.lastIndexOf(token) + token.length();
	}
}
