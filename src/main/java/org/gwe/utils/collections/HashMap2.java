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

package org.gwe.utils.collections;

import java.util.HashMap;

/**
 * @author Marco Ruiz
 * @since Aug 18, 2008
 */
public class HashMap2<KEY_TYPE, VALUE_TYPE> extends HashMap<KEY_TYPE, VALUE_TYPE> {
	
	public VALUE_TYPE getOrCreate(KEY_TYPE key, MapValueCreator<KEY_TYPE, VALUE_TYPE> valueCreator) {
	    VALUE_TYPE value = get(key);
	    if (value == null) {
	        value = valueCreator.createEntryValue(key);
	    	put(key, value);
	    }
	    return value;
	}
}

