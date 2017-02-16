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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Marco Ruiz
 * @since Nov 15, 2007
 */
public class LoggingConfigurer {

	private Map<String, String> levels;
	private String pattern;
	private boolean addConsoleAppender;
	private Set<String> logFiles;
	
	public void init() {
		for (String loggerName : levels.keySet()) {
			String levelName = levels.get(loggerName);
			try {
				Level level = Level.toLevel(levelName);
				Logger.getLogger(loggerName).setLevel(level);
			} catch (IllegalArgumentException e) {
				System.err.println("WARNING: Unable to parse '" + levelName + "' as a org.apache.log4j.Level for logger "
						+ loggerName + "; ignoring...");
			}
		}

		Layout layoutObj = new PatternLayout(pattern);

		if (addConsoleAppender) 
			Logger.getRootLogger().addAppender(new ConsoleAppender(layoutObj));

		for (String filename : logFiles)
			try {
				Logger.getRootLogger().addAppender(new FileAppender(layoutObj, filename));
			} catch (IOException e) {
				System.err.println("WARNING: Unable to create file appender for log file '" + filename + "'; ignoring...");
			}
	}

	public void setLevels(Map<String, String> props) {
		this.levels = props;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setAddConsoleAppender(boolean addConsoleAppender) {
		this.addConsoleAppender = addConsoleAppender;
	}

	public void setLogFiles(Set<String> logFiles) {
		this.logFiles = logFiles;
	}
	
	
/*		
		// Instantiate a layout and an appender, assign layout to appender programmatically
		SimpleLayout myLayout = new SimpleLayout();
		ConsoleAppender myAppender = new ConsoleAppender(myLayout);    // Appender is Interface
		// Assign appender to the logger programmatically
		myLogger.addAppender(myAppender);

		
		
		log4j.rootLogger=debug, stdout, R

		log4j.appender.stdout=org.apache.log4j.ConsoleAppender
		log4j.appender.stdout.layout=org.apache.log4j.PatternLayout

		# Pattern to output the caller's file name and line number.
		log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n

		log4j.appender.R=org.apache.log4j.RollingFileAppender
		log4j.appender.R.File=example.log

		log4j.appender.R.MaxFileSize=100KB
		# Keep one backup file
		log4j.appender.R.MaxBackupIndex=1

		log4j.appender.R.layout=org.apache.log4j.PatternLayout
		log4j.appender.R.layout.ConversionPattern=%p %t %c - %m%n




		<?xml version="1.0" encoding="UTF-8" ?>
		<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
		<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
		  <appender name="stdout" class="org.apache.log4j.ConsoleAppender"> 
		    <layout class="org.apache.log4j.PatternLayout"> 
		      <!-- Pattern to output the caller's file name and line number -->
		      <param name="ConversionPattern" value="%5p [%t] (%F:%L) - %m%n"/> 
		    </layout> 
		  </appender> 
		  <appender name="R" class="org.apache.log4j.RollingFileAppender"> 
		    <param name="file" value="example.log"/>
		    <param name="MaxFileSize" value="100KB"/>
		    <!-- Keep one backup file -->
		    <param name="MaxBackupIndex" value="1"/>
		    <layout class="org.apache.log4j.PatternLayout"> 
		      <param name="ConversionPattern" value="%p %t %c - %m%n"/> 
		    </layout> 
		  </appender> 
		  <root> 
		    <priority value ="debug" /> 
		    <appender-ref ref="stdout" /> 
		    <appender-ref ref="R" /> 
		  </root>
		</log4j:configuration>
*/
}
