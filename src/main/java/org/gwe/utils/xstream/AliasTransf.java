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

package org.gwe.utils.xstream;

import java.lang.reflect.Field;

import com.thoughtworks.xstream.XStream;

/**
 * @author Marco Ruiz
 * @since Jan 15, 2009
 */
public class AliasTransf<CLASS_TYPE> {
	
	private Class<CLASS_TYPE> aliasClass;
	private String aliasName;
	private String implicitCollectionName;
	private boolean attributize = true;
	
	public AliasTransf(Class<CLASS_TYPE> aliasClass, String aliasName) {
		this(aliasClass, aliasName, null);
    }
	
	public AliasTransf(Class<CLASS_TYPE> aliasClass, String aliasName, String implicitCollectionName) {
        this.aliasClass = aliasClass;
        this.aliasName = aliasName;
        this.implicitCollectionName = implicitCollectionName;
    }

	public AliasTransf<CLASS_TYPE> setAttributize(boolean attributize) {
    	this.attributize = attributize;
    	return this;
    }

	public void transform(XStream xstream) {
        xstream.alias(aliasName, aliasClass);
        if (implicitCollectionName != null) xstream.addImplicitCollection(aliasClass, implicitCollectionName);
        if (!attributize) return;
        for (Field fld : aliasClass.getDeclaredFields()) {
        	if (!fld.getName().equals(implicitCollectionName)) 
        		xstream.useAttributeFor(fld.getName(), fld.getType());
        }
	}
}

