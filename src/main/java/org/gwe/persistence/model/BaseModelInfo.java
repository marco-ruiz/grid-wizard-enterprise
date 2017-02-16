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

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * @author Marco Ruiz
 * @since Nov 14, 2007
 */
abstract class BaseModelInfoIdGenerator<INFO_TYPE extends BaseModelInfo<KEY_TYPE>, KEY_TYPE extends Serializable> implements IdentifierGenerator {

	public final Serializable generate(SessionImplementor session, Object object) throws HibernateException {
		return generateId((INFO_TYPE) object);
	}

	public abstract KEY_TYPE generateId(INFO_TYPE infoObj);
}

/**
 * @author Marco Ruiz
 * @since Oct 4, 2007
 */
@MappedSuperclass
public abstract class BaseModelInfo<KEY_TYPE> implements Serializable {
	
	private static IEventLogger eventLogger;
	
	public static void setEventLogger(IEventLogger eventLogger) {
		BaseModelInfo.eventLogger = eventLogger;
	}
	
	
	@Column
	private Timestamp whenCreated;
	
	public abstract KEY_TYPE getId();

	public Timestamp getWhenCreated() {
		return whenCreated;
	}
	
	public void stampWhenCreated() {
		whenCreated = new Timestamp(System.currentTimeMillis());
	}

	public void logCreateEvent() {
		logEvent(EventType.CREATED, getWhenCreated());
	}

	public Timestamp logEvent(EventType evType, BaseModelInfo... relatedModels) {
		return logEvent(evType, new Timestamp(System.currentTimeMillis()), relatedModels);
	}
	
	public Timestamp logEvent(EventType evType, Timestamp when, BaseModelInfo... relatedModels) {
		return (eventLogger == null) ? new Timestamp(System.currentTimeMillis()) : eventLogger.logEvent(this, evType, when, relatedModels);
	}
	
	public ModelSummary<KEY_TYPE> createModelSummaryFor(EventType ev) {
		return new ModelSummary<KEY_TYPE>(this);
	}
}

