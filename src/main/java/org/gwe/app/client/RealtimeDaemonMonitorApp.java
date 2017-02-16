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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.EventListener;
import org.gwe.api.event.Event;
import org.gwe.app.client.regular.shell.ClientShellApp;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.security.CredentialNotFoundException;

/**
 * @author Marco Ruiz
 * @since Jan 31, 2008
 */
public class RealtimeDaemonMonitorApp extends ClientShellApp {

	private static Log log = LogFactory.getLog(RealtimeDaemonMonitorApp.class);
	public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
	public static void main(String[] args) throws IOException, CredentialNotFoundException {
		new RealtimeDaemonMonitorApp(0, new ArgsList(args));
	}

	public RealtimeDaemonMonitorApp(int index, ArgsList argsList) throws CredentialNotFoundException, IOException {
		super("Realtime Monitor", ProgressTracker.CONSOLE_TRACKER, index, argsList);
		try {
			appConfig.createDaemonConfig(cluster).createAPILink().createEventMonitor().monitorEvents(new RealTimeEventListener());
		} catch (Exception e) {
			// FIXME: Try to recover gracefully from an error that will prevent to report execution progress
		}
	}

	class RealTimeEventListener extends EventListener {
		public void eventPerformed(Event ev) {
			tracker.trackProgress((RealtimeDaemonMonitorApp.formatter.format(new Date()) + " - " + ev));
		}
	}
}
