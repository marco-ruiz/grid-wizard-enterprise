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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import org.gwe.persistence.model.BaseModelInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Marco Ruiz
 * @since Aug 13, 2007
 */
public class GenericHibernateDaoSupport<INFO_TYPE extends BaseModelInfo<KEY_TYPE>, KEY_TYPE extends Serializable> extends HibernateDaoSupport {

	private Class<INFO_TYPE> infoClass;
	private Class<KEY_TYPE>  keyClass;
	
	protected String tableName;

	private String hqlCount;
	private String hqlDeleteAll;
	private String hqlFindAll;
	
	public GenericHibernateDaoSupport() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.infoClass = (Class<INFO_TYPE>) genericSuperclass.getActualTypeArguments()[0];
		this.keyClass =  (Class<KEY_TYPE>)  genericSuperclass.getActualTypeArguments()[1];
		this.tableName = infoClass.getName();
		
		this.hqlCount = "select count(*) from " + tableName + " x";
		this.hqlDeleteAll = "delete " + tableName;
		this.hqlFindAll = "from " + tableName + " x";
	}

	protected <CLASS_TYPE> Class<CLASS_TYPE> getParametrizedType(int index) {
	    ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		return (Class<CLASS_TYPE>)  genericSuperclass.getActualTypeArguments()[index];
    }

	public Class<INFO_TYPE> getInfoClass() {
		return infoClass;
	}

	public Class<KEY_TYPE> getKeyClass() {
		return keyClass;
	}

	public INFO_TYPE get(KEY_TYPE id) {
		return (INFO_TYPE) getHibernateTemplate().get(getInfoClass(), id);
	}

	public void update(INFO_TYPE infoObj) {
		getHibernateTemplate().update(infoObj);
	}

	public void updateAndEvict(INFO_TYPE infoObj) {
		getHibernateTemplate().update(infoObj);
		evict(infoObj);
	}

	public KEY_TYPE save(INFO_TYPE infoObj) {
		infoObj.stampWhenCreated();
		KEY_TYPE key = (KEY_TYPE) getHibernateTemplate().save(infoObj);
		infoObj.logCreateEvent();
		return key;
	}
	
	public void evict(INFO_TYPE infoObj) {
		getHibernateTemplate().evict(infoObj);
	}
	
	public void evictAll(List<INFO_TYPE> infoList) {
		for (INFO_TYPE infoObj : infoList) evict(infoObj);
	}
	
	public INFO_TYPE merge(INFO_TYPE infoObj) {
		return (INFO_TYPE) getHibernateTemplate().merge(infoObj);
	}

	public KEY_TYPE saveOrUpdate(INFO_TYPE infoObj) {
		if (get(infoObj.getId()) == null) return save(infoObj);
		update(infoObj);
		return infoObj.getId();
	}

	public void saveAll(Collection<INFO_TYPE> infoList) {
		for (INFO_TYPE infoObj : infoList) infoObj.stampWhenCreated();
		saveOrUpdateAll(infoList);
		for (INFO_TYPE infoObj : infoList) infoObj.logCreateEvent();
	}

	public void saveOrUpdateAll(Collection<INFO_TYPE> infoList) {
		if (infoList != null && !infoList.isEmpty()) getHibernateTemplate().saveOrUpdateAll(infoList);
	}

	public void delete(INFO_TYPE infoObj) {
		getHibernateTemplate().delete(infoObj);
	}

	public List<INFO_TYPE> getList() {
		return (getHibernateTemplate().find(hqlFindAll));
	}
	
	public List<INFO_TYPE> getForFieldIn(final String idPropName, final Collection<?> ids) {
		return (List<INFO_TYPE>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.createCriteria(infoClass)
				.add(Restrictions.in(idPropName, ids))
				.list();
			}
		});
	}
		
	public void deleteById(KEY_TYPE id) {
		Object obj = get(id);
		getHibernateTemplate().delete(obj);
	}

	public void deleteAll(Collection<INFO_TYPE> entities) {
		getHibernateTemplate().deleteAll(entities);
	}

	public void deleteAll() {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				int deletedEntities = session.createQuery(hqlDeleteAll).executeUpdate();
				return null;
			}
		});
	}

	public int count() {
		return (Integer)queryFirst(hqlCount);
	}
	
	public <T> T queryFirst(String hql) {
		return (T) query(hql).get(0);
	}

	public <T> List<T> query(String hql) {
		return (List<T>) getHibernateTemplate().find(hql);
	}
}
