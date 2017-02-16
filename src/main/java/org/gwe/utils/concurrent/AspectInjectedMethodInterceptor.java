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

package org.gwe.utils.concurrent;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Jul 2, 2008
 */
class AspectInjectedMethodInterceptor<PROXIED_TYPE> implements MethodInterceptor {

	private static Log log = LogFactory.getLog(AspectInjectedMethodInterceptor.class);
	
    private final Class<PROXIED_TYPE> proxiedClass;
    
    AspectInjectedMethodInterceptor(Class<PROXIED_TYPE> proxiedClass) {
		this.proxiedClass = proxiedClass;
    }
    
    public Object invoke(MethodInvocation invocation) throws Throwable {
		return invokeBareMethod(invocation);
    }
    
	protected Object invokeBareMethod(MethodInvocation invocation) throws Throwable {
    	Object id = invocation.getArguments()[0];
    	String methodName = invocation.getMethod().getName();
        log.info("AOP for " + proxiedClass + ". Invoking '" + methodName + "'  for entity '" + id + "' with arguments (" + argsToString(invocation) + ") ...");
        Object result = invocation.proceed();
    	log.info("AOP for " + proxiedClass + ". '" + methodName + "' invoked for entity '" + id + "' ! Return value: \n" + result);
        return result;
    }

	private String argsToString(MethodInvocation invocation) {
        String args = "";
        Object[] array = invocation.getArguments();
        for (int idx = 1; idx < array.length; idx++) args += array[idx] + ";";
        return args;
    }
}

