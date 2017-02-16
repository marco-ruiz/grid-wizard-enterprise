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

package org.gwe.app.daemon;

/**
 * @author Marco Ruiz
 * @since Feb 27, 2008
 */
public class DaemonBeacon implements Runnable {

	private boolean stop = false;
	
	public DaemonBeacon() {
		Thread beaconThread = new Thread(this);
		beaconThread.setDaemon(true);
		beaconThread.start();
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void run() {
		while (!stop) {
			System.out.print(".");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
		}
	}
}
