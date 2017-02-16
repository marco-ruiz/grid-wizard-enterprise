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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.AllocationInfo;


/**
 * @author Marco Ruiz
 * @since Sep 06, 2007
 */
public class AllocationInfoDAO extends BaseInfoDAO<AllocationInfo, Integer>{
	private static Log log = LogFactory.getLog(AllocationInfoDAO.class);

	public AllocationInfoDAO() {}

/*
	public List<AllocationInfo> getLiveAllocations() {
		return getSession().createCriteria(AllocationInfo.class)
			.add(Restrictions.isNull("whenReleased"))
			.addOrder(Order.asc("whenCreated"))
			.list();
	}

	public List<AllocationInfo> getReadyAllocations() {
		return getSession().createCriteria(AllocationInfo.class)
			.add(Restrictions.isNotNull("whenAttained"))
			.add(Restrictions.isNull("whenReleased"))
			.addOrder(Order.asc("whenCreated"))
			.list();
	}

	public List<AllocationInfo> getWaitingAssignmentAllocations() {
		return getSession().createCriteria(AllocationInfo.class)
			.add(Restrictions.isNotNull("whenAttained"))
			.add(Restrictions.isNull("whenReleased"))
			.add(Restrictions.isNull("jobToExecute"))
			.addOrder(Order.asc("whenCreated"))
			.list();
	}
*/
}

