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

import org.gwe.persistence.model.OrderInfo;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * @author Marco Ruiz
 * @since Aug 13, 2007
 */
public class OrderInfoDAO extends BaseInfoDAO<OrderInfo, Integer> {
	
	public List<OrderInfo> getByDescription(final String description) {
		return (List<OrderInfo>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Criteria criteria = session.createCriteria(OrderInfo.class);
				if (description != null) criteria = criteria.add(Restrictions.eq("description", description));
				return criteria.list();
			}
		});
	}

	public OrderInfo getOrder(int orderId, boolean includeJobs) {
		OrderInfo result = get(orderId);
		if (includeJobs && result != null) result.getJobs().size();
		return result;
	}
	
	public Integer save(OrderInfo order) {
		Integer result = super.save(order);
		order.setPriority(order.getId());
		return result;
	}

	public List<OrderInfo> getList() {
		return (List<OrderInfo>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.createCriteria(OrderInfo.class)
					.add(Restrictions.eq("deleted", false))
//					.addOrder(Order.asc("priority"))
					.list();
			}
		});
	}

	public void swapPriorities(final int orderId1, final int offset) {
		final int priority = get(orderId1).getPriority();
		if (priority < 1) return;
/*
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
	            Query query = session.createQuery("from OrderInfo where priority = :priority");
		        query.setInteger("priority", priority);
	            List<OrderInfo> orders = query.list();
	            int otherId = -1;
				if (!orders.isEmpty()) otherId = orders.get(0).getId();

	            
				setPriority(session, orderId2, priority);
				setPriority(session, orderId1, priority + offset);
		        return null;
			}

			private void setPriority(Session session, final int orderId1, final int priority) {
	            Query query = session.createQuery("update OrderInfo set priority = :priority where id = :id");
		        query.setInteger("id", orderId1);
		        query.setInteger("priority", priority);
		        query.executeUpdate();
            }
		});
*/
    }
	
	public void markAsDeleted(final int orderId) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
		        Query query = session.createQuery("update OrderInfo set deleted = true, paused = true where id = :id");
		        query.setInteger("id", orderId);
		        return query.executeUpdate();
			}
		});
    }

	public void setPause(final int orderId, final boolean pause) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
		        Query query = session.createQuery("update OrderInfo set paused = :pause where id = :id");
		        query.setInteger("id", orderId);
		        query.setBoolean("pause", pause);
		        return query.executeUpdate();
			}
		});
    }
}

