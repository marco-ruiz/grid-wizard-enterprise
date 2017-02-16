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
 * @since Jan 18, 2008
 */
public class JobExecutionInfoIdGenerator extends BaseModelInfoIdGenerator<JobExecutionInfo, String> {
	
    public String generateId(JobExecutionInfo infoObj) {
		return InfoUtils.generateId(infoObj.getJob().getId(), infoObj.getExecutionNum());
    }
}
