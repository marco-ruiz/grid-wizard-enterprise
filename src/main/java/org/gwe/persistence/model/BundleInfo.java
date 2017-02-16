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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity representing bundle to install in the cluster prior to execute its associated owner's order.
 * 
 * @author Marco Ruiz
 * @since Feb 3, 2008
 */
@Entity
public class BundleInfo extends BaseModelInfo<Integer> {

    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO) 
	private int id;
	
	private String variableName;
	private String description;
	
	public Integer getId() {
		return id;
	}
}
