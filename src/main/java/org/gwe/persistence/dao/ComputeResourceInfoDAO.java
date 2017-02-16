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

import org.gwe.persistence.model.ComputeResourceInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;


/**
 * @author Marco Ruiz
 * @since Sep 06, 2007
 */
public class ComputeResourceInfoDAO extends BaseInfoDAO<ComputeResourceInfo, String>{

	public void saveOrUpdateAndFlush(final ComputeResourceInfo compRes) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				ComputeResourceInfo persistedCompRes = (ComputeResourceInfo) session.merge(compRes);
				session.saveOrUpdate(persistedCompRes);
				session.flush();
				return null;
			}
		});
	}
}
