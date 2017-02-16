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

package org.gwe.utils;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Marco Ruiz
 * @since Nov 15, 2007
 */
public class LoggingLevelsConfigurer {

    private static Log log = LogFactory.getLog(LoggingLevelsConfigurer.class);

	private Map<Class, Map<String, String>> loggingLevels;
	
	public LoggingLevelsConfigurer(Map<Class, Map<String, String>> loggingLevels) {
	    this.loggingLevels = loggingLevels;
    }

	public void applyLevelsFor(Class appClass) {
		Map<String, String> levels = loggingLevels.get(appClass);
		if (levels == null) {
			log.info("No logging levels found for class '" + appClass + "'!");
			return;
		}
		for (String loggerName : levels.keySet()) {
			String levelName = levels.get(loggerName);
			try {
				Level level = Level.toLevel(levelName);
				Logger.getLogger(loggerName).setLevel(level);
				log.info("Logger '"  + loggerName + "' was set to level '" + level + "'!");
			} catch (Exception e) {
				log.warn("Unable to set logger '"  + loggerName + "' level to '" + levelName + "'!", e);
				System.err.println(e);
			}
		}
	}
}
