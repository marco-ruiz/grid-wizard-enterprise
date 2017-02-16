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

package org.gwe.persistence.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gwe.persistence.model.JobExecutionInfo;

/**
 * @author Marco Ruiz
 * @since Aug 13, 2007
 */
public class JobExecutionInfoDAO extends BaseInfoDAO<JobExecutionInfo, String> {

	public List<JobExecutionInfo> getBelongingToJob(String jobId) {
		Set<String> idsCollection = new HashSet<String>();
		Collections.addAll(idsCollection, jobId);
		return getForFieldIn("job.id", idsCollection);
    }
}

