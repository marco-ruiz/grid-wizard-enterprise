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

package org.gwe.drivers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since Aug 9, 2007
 */
public class DriversManager<DRIVER_TYPE extends Driver<HANDLE_TYPE, HANDLE_PARAM_TYPE>, HANDLE_TYPE, HANDLE_PARAM_TYPE> {
	
    private static Log log = LogFactory.getLog(DriversManager.class);

	protected List<DRIVER_TYPE> drivers;
	private String localHostName = "localhost";

    public DriversManager(List<DRIVER_TYPE> drivers) {
    	if (drivers == null) return;
    	this.drivers = new ArrayList<DRIVER_TYPE>(drivers.size());
    	this.drivers.addAll(drivers);
		try {
	        this.localHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
	        // Do nothing. Take default "localhost" string as the localhost name
        }
    }
    
	public void setDrivers(List<DRIVER_TYPE> drivers) {
		this.drivers = drivers;
	}

	public String getLocalHostName() {
    	return localHostName;
    }

	public void setLocalHostName(String localHost) {
    	this.localHostName = localHost;
    }
/*
	public HANDLE_TYPE createHandle() throws HandleCreationException {
		return createHandle(null);
	}
*/
	public HANDLE_TYPE createHandle(HANDLE_PARAM_TYPE handleParams) throws HandleCreationException {
		for (DRIVER_TYPE driver : drivers) {
			try {
				return driver.tryToCreateHandle(handleParams);
			} catch(HandleCreationNotSupportedException e) {
			} catch(Exception e) {
				String msg = "Problem encountered while trying to create handle with params " + handleParams + 
					" using driver '" + getClass().getName() + "'. Exception: " + e.getMessage();
				log.warn(msg, e.getCause());
			}
		}
		throw new HandleCreationException("There was no driver able to create a handle for '" + handleParams + "'");
	}
}
