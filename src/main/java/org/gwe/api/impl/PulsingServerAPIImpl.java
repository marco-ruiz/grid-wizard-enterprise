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

package org.gwe.api.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.api.PulsingServerAPI;
import org.gwe.api.PulsingServerAPIProxyCreator;
import org.gwe.app.daemon.domain.BaseDomain;
import org.gwe.utils.concurrent.HeartBeater;

/**
 * @author Marco Ruiz
 * @since Nov 21, 2007
 */
public abstract class PulsingServerAPIImpl<API_TYPE extends PulsingServerAPI, DOM_TYPE extends BaseDomain> 
	extends BaseServerAPIImpl<API_TYPE, DOM_TYPE> implements PulsingServerAPI {

	private static Log log = LogFactory.getLog(PulsingServerAPIImpl.class);

	// Heart beat checker aspect with a 30 seconds grace period
	private PulsingServerAPIProxyCreator<PulsingServerAPIImpl<API_TYPE, DOM_TYPE>> beatingCheckerAspect = 
		new PulsingServerAPIProxyCreator<PulsingServerAPIImpl<API_TYPE, DOM_TYPE>>(new BeatChecker(), PulsingServerAPI.DEFAULT_HEART_BEAT_PERIOD + 30000);

	protected Remote createExportableServerObject() {
		return beatingCheckerAspect.createProxy(super.createExportableServerObject());
	}

	public final void heartBeat(Object id) throws RemoteException {
		log.info("Heart beat received for entity '" + id + "'");
		beatingCheckerAspect.resetLastHeartBeat(id, this);
	}
	
	public final void disposeHeart(Object id) {
		beatingCheckerAspect.stop(id);
	}
	
	public abstract void processHeartBeatOverdue(Object id); 
	
	class BeatChecker implements HeartBeater<PulsingServerAPIImpl<API_TYPE, DOM_TYPE>> {
		// If it ever beats it means it had an overdue beat
		public boolean beatAndReportIfMustContinue(Object id, PulsingServerAPIImpl<API_TYPE, DOM_TYPE> heartContainer) {
			heartContainer.processHeartBeatOverdue(id);
			return false;
		}
	} 
}
