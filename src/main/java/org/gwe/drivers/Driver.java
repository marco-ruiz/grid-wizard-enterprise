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

package org.gwe.drivers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Marco Ruiz
 * @since Aug 9, 2007
 */
public abstract class Driver<HANDLE_TYPE, HANDLE_PARAMS> {

	public Type[] getTypeArgs() {
		return (Type[])((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
	}

	public Class<HANDLE_TYPE> getHandleTypeClass() {
		return (Class<HANDLE_TYPE>) getTypeArgs()[0];
	}

	public Class<HANDLE_PARAMS> getHandleParamsClass() {
		return (Class<HANDLE_PARAMS>) getTypeArgs()[1];
	}

	public HANDLE_TYPE tryToCreateHandle(HANDLE_PARAMS handleParams) throws Exception {
		return getHandleTypeClass().getConstructor(getHandleParamsClass()).newInstance(new Object[] { handleParams });
	}
}

