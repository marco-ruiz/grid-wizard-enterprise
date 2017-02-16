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

package org.gwe.utils.reinvoke;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * @author Marco Ruiz
 * @since Nov 23, 2008
 */
public class ReinvocationInterceptor implements MethodInterceptor {
    
	private static Log log = LogFactory.getLog(ReinvocationInterceptor.class);

	public static <PROXIED_TYPE> PROXIED_TYPE createProxy(PROXIED_TYPE target) {
		ReinvocationInterceptor interceptor = new ReinvocationInterceptor();
		ProxyFactory factory = new ProxyFactory(target);
		factory.addAdvice(interceptor);
		return (PROXIED_TYPE)factory.getProxy();
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
    	Reinvoke reinvokeAnot = invocation.getMethod().getAnnotation(Reinvoke.class);
    	if (reinvokeAnot != null) {
            for (int trials = reinvokeAnot.times(); trials > 0; trials--) {
		    	try {
		    		return invocation.proceed();
		    	} catch (Exception e) {
	            	if (isReinvocationException(reinvokeAnot.onExceptions(), e)) {
			    		log.info("Exception '" + e.getClass() + "' supported for reinvocation thrown while invoking '" + invocation.getMethod() + "'. Trials left: " + trials, e);
	            	} else {
			    		log.info("Exception '" + e.getClass() + "' NOT supported for reinvocation thrown while invoking '" + invocation.getMethod() + "'. RETHROWING THE EXCEPTION!", e);
			    		throw e;
	            	}
		    	}
		    	sleep(reinvokeAnot.periodInBetween());
		    }
    	}
		return invocation.proceed();
    }

	private boolean isReinvocationException(Class<? extends Exception>[] classes, Exception e) {
	    for (Class<? extends Exception> exc : classes) 
	    	if (exc.isAssignableFrom(e.getClass())) return true;
	    return false;
    }

	private void sleep(int sleepPeriod) {
	    try {
	    	Thread.sleep(sleepPeriod);
	    } catch (Exception e) {}
	    Thread.yield();
    }
}
