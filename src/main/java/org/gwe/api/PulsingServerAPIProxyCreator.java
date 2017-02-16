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

package org.gwe.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.concurrent.Heart;
import org.gwe.utils.concurrent.HeartBeater;
import org.gwe.utils.concurrent.HeartCollection;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;

/**
 * @author Marco Ruiz
 * @since Nov 24, 2007
 */
public class PulsingServerAPIProxyCreator<HEART_CONTAINER_TYPE> {

	private static Log log = LogFactory.getLog(PulsingServerAPIProxyCreator.class);

	private static final Pointcut pointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method method, Class targetClass) {
			boolean preliminaryTest = 
				PulsingServerAPI.class.isAssignableFrom(targetClass) && 
				Modifier.isPublic(method.getModifiers()) &&
				!method.getName().equals("heartBeat");
			
			if (preliminaryTest) {
				for (Class<?> excType : method.getExceptionTypes())
					if (RemoteException.class.isAssignableFrom(excType)) return true;
			}
			
			return false; 
		}
	};
	
	public static <PROXIED_TYPE> PROXIED_TYPE createProxyIfNecessary(PROXIED_TYPE proxiedObject, HeartBeater<?> beater) {
		return ((proxiedObject instanceof PulsingServerAPI) && beater != null) ? 
				(PROXIED_TYPE) new PulsingServerAPIProxyCreator(beater).createProxy(proxiedObject) : proxiedObject;
	}
	
	private HeartCollection<HEART_CONTAINER_TYPE> hearts;

	public PulsingServerAPIProxyCreator(HeartBeater<HEART_CONTAINER_TYPE> beater) {
		this(beater, PulsingServerAPI.DEFAULT_HEART_BEAT_PERIOD);
	}
	
	public PulsingServerAPIProxyCreator(HeartBeater<HEART_CONTAINER_TYPE> beater, int period) {
		this(new HeartCollection<HEART_CONTAINER_TYPE>(beater, period));
	}
	
	public PulsingServerAPIProxyCreator(HeartCollection<HEART_CONTAINER_TYPE> hearts) {
		this.hearts = hearts;
	}
	
	public void stop(Object id) {
		hearts.getHeart(id, null).stop();
	}
	
	public void resetLastHeartBeat(Object id, HEART_CONTAINER_TYPE heartContainer) {
		hearts.getHeart(id, heartContainer).resetLastHeartBeat();
	}
	
	public <PROXIED_TYPE> PROXIED_TYPE createProxy(final PROXIED_TYPE proxiedObject) {
		ProxyFactory factory = new ProxyFactory(proxiedObject);
		factory.addAdvisor(new DefaultPointcutAdvisor(pointcut, new PulsingMethodInterceptor<PROXIED_TYPE>(proxiedObject)));
		return (PROXIED_TYPE)factory.getProxy();
	}

    class PulsingMethodInterceptor<PROXIED_TYPE> implements MethodInterceptor {
	    private final PROXIED_TYPE proxiedObject;
	    private final Class<? extends Object> proxiedClass;
	    
	    private PulsingMethodInterceptor(PROXIED_TYPE proxiedObject) {
		    this.proxiedObject = proxiedObject;
			this.proxiedClass = proxiedObject.getClass();
	    }
	    
	    public Object invoke(MethodInvocation invocation) throws Throwable {
	    	Object id = invocation.getArguments()[0];
			Heart<HEART_CONTAINER_TYPE> heart = hearts.getHeart(id, (HEART_CONTAINER_TYPE) invocation.getThis());
	    	heart.pause();
	    	String methodName = invocation.getMethod().getName();
			log.info("AOP for " + proxiedClass + ". Invoking '" + methodName + "'  for entity '" + id + "' with arguments (" + argsToString(invocation) + ") ...");
	        Object result = invocation.proceed();
	    	log.info("AOP for " + proxiedClass + ". '" + methodName + "' invoked for entity '" + id + "' ! Return value: \n" + result);
	    	heart.resume();
	        return result;
	    }

		private String argsToString(MethodInvocation invocation) {
	        String args = "";
	        Object[] array = invocation.getArguments();
	        for (int idx = 1; idx < array.length; idx++) args += array[idx] + ";";
	        return args;
        }
    }
}
