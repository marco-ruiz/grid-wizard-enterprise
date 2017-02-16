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

/**
 * @author Marco Ruiz
 * @since Sep 15, 2008
 */
public class InfoUtils {
	
	private static final String IDS_SEPARATOR = "_";

	public static String generateId(Object... pieces) {
		if (pieces == null || pieces.length == 0) return null;
		String result = pieces[0].toString();
		for (int idx = 1; idx < pieces.length; idx++) result += IDS_SEPARATOR + pieces[idx];
		return result; 
	}

	public static String[] getIdPieces(BaseModelInfo<?> infoObj) {
		return infoObj.getId().toString().split(IDS_SEPARATOR);
	}
}
