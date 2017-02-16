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

package org.gwe.app.client;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Oct 2, 2008
 */
public interface ProgressTracker {
	
	public static final Log log = LogFactory.getLog(ProgressTracker.class);

	public static ProgressTracker SILENT_TRACKER = new ProgressTracker() {
		public void trackProgress(String msg) {}
	};
	
	public static ProgressTracker LOG_TRACKER = new ProgressTracker() {
		public void trackProgress(String msg) {
			log.info("PROGRESS REPORT: " + msg);
		}
	};
	
	public static ProgressTracker CONSOLE_TRACKER = new ProgressTracker() {
		public void trackProgress(String msg) {
			OutputStream os = System.out;
			if (os != null) {
	        	try {
	        		os.write((msg + "\n").getBytes());
	        	} catch (IOException e) {
	        	}
	        }
		}
	};
	
	public void trackProgress(String msg);
}
