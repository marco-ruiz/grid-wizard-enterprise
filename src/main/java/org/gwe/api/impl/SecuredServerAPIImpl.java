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

package org.gwe.api.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.gwe.api.SecuredServerAPI;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.daemon.domain.BaseDomain;
import org.gwe.utils.security.AccountInfo;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * @author Marco Ruiz
 * @since Nov 21, 2007
 */
public abstract class SecuredServerAPIImpl<API_TYPE extends SecuredServerAPI, DOM_TYPE extends BaseDomain> 
	extends BaseServerAPIImpl<API_TYPE, DOM_TYPE> implements SecuredServerAPI {

	private static final Pointcut pointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method method, Class targetClass) {
			boolean preliminaryTest = 
				SecuredServerAPI.class.isAssignableFrom(targetClass) && 
				Modifier.isPublic(method.getModifiers()) &&
				method.getParameterTypes()[0].equals(AccountInfo.class) &&
				!method.getName().equals("verifyAccount");
			
			if (preliminaryTest) {
				for (Class<?> excType : method.getExceptionTypes())
					if (RemoteException.class.isAssignableFrom(excType)) return true;
			}
			
			return false; 
		}
	};
	
	private static final MethodInterceptor interceptor = new MethodInterceptor() {
	    public Object invoke(MethodInvocation invocation) throws Throwable {
	    	SecuredServerAPI thisAPI = (SecuredServerAPI)invocation.getThis();
			thisAPI.verifyAccount((AccountInfo) invocation.getArguments()[0]);
	        return invocation.proceed();
	    }
	};
	
	protected Remote createExportableServerObject() {
		ProxyFactory factory = new ProxyFactory(super.createExportableServerObject());
        factory.addAdvisor(new DefaultPointcutAdvisor(pointcut, interceptor));
        return (Remote)factory.getProxy();
	}

	public final void verifyAccount(AccountInfo applyingAuth) throws RemoteException, PasswordMismatchException {
		domain.verifyAccount(applyingAuth);
	}
}
