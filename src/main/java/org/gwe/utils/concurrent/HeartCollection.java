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

package org.gwe.utils.concurrent;

import java.util.HashMap;

/**
 * @author Marco Ruiz
 * @since Sep 30, 2008
 */
public class HeartCollection<HEART_CONTAINER_TYPE> extends HashMap<Object, Heart<HEART_CONTAINER_TYPE>>{
	
	private HeartBeater<HEART_CONTAINER_TYPE> beater;
	private int period;

	public HeartCollection(HeartBeater<HEART_CONTAINER_TYPE> beater, int period) {
		this.beater = beater;
		this.period = period;
	}
	
    public Heart<HEART_CONTAINER_TYPE> getHeart(Object id, HEART_CONTAINER_TYPE heartContainer) {
    	Heart<HEART_CONTAINER_TYPE> result = get(id);
    	if (result == null) {
	    	result = new Heart<HEART_CONTAINER_TYPE>(id, heartContainer, beater, period);
	    	put(id, result);
    	}
    	return result;
    }
}
