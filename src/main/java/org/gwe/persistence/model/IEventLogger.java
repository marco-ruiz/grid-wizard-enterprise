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

import java.sql.Timestamp;


/**
 * @author Marco Ruiz
 * @since Oct 8, 2007
 */
public interface IEventLogger {
	
	public <KEY_TYPE> Timestamp logEvent(BaseModelInfo<KEY_TYPE> source, EventType evType, Timestamp when, BaseModelInfo... relatedModels);

}
