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

package org.gwe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.gwe.app.Distribution;
import org.gwe.drivers.ResourceHandleFactory;
import org.gwe.drivers.fileSystems.GridFileSystem;
import org.gwe.p2elv2.PFunction;
import org.gwe.persistence.model.BaseModelInfo;
import org.gwe.persistence.model.CompositeEventLogger;
import org.gwe.persistence.model.IEventLogger;
import org.gwe.utils.IOUtils;
import org.gwe.utils.LoggingLevelsConfigurer;
import org.gwe.utils.security.ResourceLink;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @author Marco Ruiz
 * @since Jul 5, 2007
 */
public class GWEAppContext {
	
	public static final String DEFAULT_CORE_CONF = "spring-gwe-core.xml";
	private static final String DEFAULT_LOG_CONF = "spring-gwe-log.xml";

	private static final String PROP_GWE_HOME 	 = "gwe.home";
	public static final PatternLayout LOG_LAYOUT = new PatternLayout("%-10d{HH:mm:ss} [%t] %-5p  - %c{1}:%L %x - %m%n");
	
	protected static GWEAppContext instance = null;
	
	public static GridFileSystem getGridFileSystem() {
		return instance.gridFileSystem;
	}
	
	public static List<String> getP2ELFunctionNames() {
		List<String> result = new ArrayList<String>();
		for (PFunction function : instance.p2elFunctions) result.add(function.getName());
		return result;
	}
	
	protected AbstractApplicationContext appContext;

	protected GridFileSystem gridFileSystem;
	protected List<PFunction> p2elFunctions;
	
	private String installPath;

	public GWEAppContext(Class appClass, String installPath, String workspace, String... ctxFiles) {
		this.installPath = installPath;
        System.setProperty(PROP_GWE_HOME, installPath);
		createLoggerAppender(workspace, true);
		
		configureLoggers(appClass);
		
		ctxFiles = addCoreContextIfNotPresent(ctxFiles);
		for (int idx = 0; idx < ctxFiles.length; idx++) 
			ctxFiles[idx] = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ctxFiles[idx];
		
		try {
			appContext     = new ClassPathXmlApplicationContext(ctxFiles);
			gridFileSystem = getBeanOfClass(GridFileSystem.class);
			p2elFunctions  = getBean("p2elFunctions");
			ResourceHandleFactory handleFactory = getBean("resourceHandleFactory");
			ResourceLink.setHandleFactory(handleFactory);
			
			// Collect event loggers
			setGlobalEventLogger();
		} catch(Throwable e) {
			LogFactory.getLog(GWEAppContext.class).fatal(e);
		}
		instance = this; 
	}

	private void configureLoggers(Class appClass) {
	    AbstractApplicationContext logContext = new ClassPathXmlApplicationContext(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + DEFAULT_LOG_CONF);
	    ((LoggingLevelsConfigurer)logContext.getBean("loggingLevelsConfigurer")).applyLevelsFor(appClass);
    }
	
	private void setGlobalEventLogger() {
	    Collection<IEventLogger> eventLoggers = appContext.getBeansOfType(IEventLogger.class).values();
	    CompositeEventLogger eventLogger = new CompositeEventLogger();
	    for (IEventLogger evLogger : eventLoggers) 
	    	eventLogger.addEventLogger(evLogger);
	    BaseModelInfo.setEventLogger(eventLogger);
    }
	
	private String[] addCoreContextIfNotPresent(String... contextFiles) {
		List<String> allCtxFiles = new ArrayList<String>();
		allCtxFiles.addAll(Arrays.asList(contextFiles));
		if (allCtxFiles.size() < 1 || !allCtxFiles.get(0).equals(DEFAULT_CORE_CONF)) {
			allCtxFiles.remove(DEFAULT_CORE_CONF);
			allCtxFiles.add(0, DEFAULT_CORE_CONF);
		}
		return allCtxFiles.toArray(new String[]{});
	}
	
	public String getInstallPath() {
    	return installPath;
    }

	protected static void createLoggerAppender(String workspace, boolean production) {
		String appLog = IOUtils.concatenatePaths(workspace, "app.log");
		try {
			Level level;
			if (production) {
				Logger.getRootLogger().removeAllAppenders();
				level = Level.INFO;
			} else {
				Logger.getRootLogger().addAppender(new ConsoleAppender());
				level = Level.ALL;
			}
			Logger.getRootLogger().addAppender(new FileAppender(LOG_LAYOUT, appLog));
            Logger.getRootLogger().setLevel(level);
		} catch (IOException e) {
			System.err.println("Couldn't create file log appender '" + appLog + "'");
			e.printStackTrace(System.err);
		}
	}

	public final AbstractApplicationContext getAppContext() { return appContext; }

	public final <T> T getBean(String beanName) { return (T)appContext.getBean(beanName); }
	
	public final <T> T getBeanOfClass(Class<T> beanClass) { 
		Collection<T> values = appContext.getBeansOfType(beanClass).values();
		return (T)values.iterator().next(); 
	}

	public Distribution getDistribution() {
	    return getBeanOfClass(Distribution.class);
    }
}

