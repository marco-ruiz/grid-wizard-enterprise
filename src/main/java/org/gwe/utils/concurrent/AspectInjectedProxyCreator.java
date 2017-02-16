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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * @author Marco Ruiz
 * @since Jul 2, 2008
 */
public class AspectInjectedProxyCreator<PROXIED_TYPE> {
	
	private static Log log = LogFactory.getLog(AspectInjectedProxyCreator.class);

	public <PROXIED_TYPE> PROXIED_TYPE createProxy(Class<PROXIED_TYPE> proxiedClass, Pointcut pointcut) {
		ProxyFactory factory = new ProxyFactory(new Class[]{proxiedClass});
		factory.addAdvisor(new DefaultPointcutAdvisor(pointcut, new AspectInjectedMethodInterceptor<PROXIED_TYPE>(proxiedClass)));
		return (PROXIED_TYPE)factory.getProxy();
	}
}
