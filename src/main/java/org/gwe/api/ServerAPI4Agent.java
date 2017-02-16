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

import java.io.Serializable;
import java.rmi.RemoteException;

import org.gwe.persistence.model.ComputeResourceInfo;
import org.gwe.persistence.model.order.DaemonRequest;
import org.gwe.utils.reinvoke.Reinvoke;

/**
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
public interface ServerAPI4Agent extends PulsingServerAPI {
	
	@Reinvoke(times = 5, onExceptions = {RemoteException.class})
	public boolean reserveAllocation(int allocationId, ComputeResourceInfo compRes) throws RemoteException;

	public DaemonRequest<?> getNextRequest(int allocId) throws RemoteException;

	@Reinvoke(times = 5, onExceptions = {RemoteException.class})
	public DaemonRequest<?> getNextRequestAgain(int allocId) throws RemoteException;

	@Reinvoke(times = 5, onExceptions = {RemoteException.class})
	public boolean reportRequestCompletion(int allocationId, String execId, Serializable result) throws RemoteException;
	
	public boolean reportAgentProblem(int allocationId, Exception exc) throws RemoteException;
}
