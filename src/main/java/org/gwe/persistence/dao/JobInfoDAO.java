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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.JobInfo;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * @author Marco Ruiz
 * @since Aug 13, 2007
 */
public class JobInfoDAO extends BaseInfoDAO<JobInfo, String> {

	private static Log log = LogFactory.getLog(JobInfoDAO.class);

	public void updateJob(final JobInfo job) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				// Update job in DB
//		        Query query = session.createQuery("update JobInfo set execution = :execution, failures = :failures, request = :request where id = :id");
		        Query query = session.createQuery("update JobInfo set execution = :execution, failures = :failures where id = :id");
		        query.setString("id", job.getId());
		        query.setParameter("execution", job.getExecution());
		        query.setInteger("failures", job.getFailures());
//		        query.setParameter("request", job.getRequest(), Hibernate.BLOB);
		        return query.executeUpdate();
			}
		});
	}
	
	public List<JobInfo> getNextSchedulableJobsBatch(final int size) {
		return (List<JobInfo>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List<JobInfo> result = getPendingJobsCriteria(session)
//					.addOrder(Order.asc("theOrder.priority"))
					.addOrder(Order.asc("theOrder.id"))
					.addOrder(Order.asc("failures"))
					.addOrder(Order.asc("id"))
					.setMaxResults(size)
					.list();
				return result;
			}
		});
	}

	public int getPendingJobsCount() {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Integer doInHibernate(Session session) throws HibernateException {
				return getCount(getPendingJobsCriteria(session)); 
			}
		});
	}
	
	private int getCount(Criteria criteria) {
		ProjectionList projections = Projections.projectionList()
			.add(Projections.rowCount());
		criteria.setProjection(projections);
		return ((Integer)criteria.list().get(0)).intValue();
	}

	private Criteria getPendingJobsCriteria(Session session) {
	    return session.createCriteria(JobInfo.class)
	    	.createAlias("order", "theOrder")
//	    	.createAlias("order.executionProfile", "theExecutionProfile")
			.add(Restrictions.eq("theOrder.paused", false))
			.add(Restrictions.eq("theOrder.aborted", false))
//			.add(Restrictions.gtProperty("theExecutionProfile.maxRetries", "failures"))
			.add(Restrictions.isNull("execution"));
    }
}

