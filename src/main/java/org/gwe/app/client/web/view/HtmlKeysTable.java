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

package org.gwe.app.client.web.view;

import java.util.HashMap;
import java.util.Map;

import org.gwe.app.client.config.ClientConfig;
import org.gwe.utils.security.AccessControl;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.Realm;
import org.gwe.utils.security.RealmTestResult;
import org.gwe.utils.web.HtmlTable;
import org.gwe.utils.web.HtmlTableCell;
import org.gwe.utils.web.WebIcon;

/**
 * @author Marco Ruiz
 * @since Dec 21, 2008
 */
public class HtmlKeysTable extends HtmlTable {

	private static Map<RealmTestResult, WebIcon> STATUS_IMAGES = new HashMap<RealmTestResult, WebIcon>();
	
	static {
		STATUS_IMAGES.put(RealmTestResult.OK, WebIcon.STATUS_OK);
		STATUS_IMAGES.put(RealmTestResult.ERROR, WebIcon.STATUS_ERROR);
		STATUS_IMAGES.put(RealmTestResult.NO_SSH_REALM, WebIcon.STATUS_HELP);
		STATUS_IMAGES.put(RealmTestResult.MISSING_TEST_HOST, WebIcon.STATUS_HELP);
		STATUS_IMAGES.put(RealmTestResult.MISSING_ACCOUNT, WebIcon.STATUS_WARN);
		STATUS_IMAGES.put(RealmTestResult.MISSING_PASSKEY, WebIcon.STATUS_WARN);
	}
	
	public HtmlKeysTable(ClientConfig config) {
	    super("User Name", "Authentication", "Domains", "Test Host", "");
	    
	    KeyStore keys = config.getKeys();
		for (AccessControl ac : keys.getAccessControls()) {
	        for (Realm realm : ac.getRealms()) {
	        	HtmlTableCell status = new HtmlTableCell("Tested!", "", null, STATUS_IMAGES.get(realm.getTestResult()));
		    	addRow(realm.getAccount().getUser(), getAuth(realm), realm.getDomain(), realm.getTestHost(), status);
            }
        }
    }
/*	
	private StatusImage getStatusImage(KeyStore keys, Realm realm) {
	    // Missing authentication
		if (missingAuth(realm)) 
	    	return StatusImage.ERROR; 

		// No host to test against
	    if (!realm.acceptsScheme(ProtocolScheme.SSH) || realm.getTestHost() == null || "".equals(realm.getTestHost())) 
	    	return StatusImage.HELP;  
    	
        try {
            keys.createHostLink(ProtocolScheme.SSH.toURIStr(realm.getTestHost())).createHandle().close();
        	// Authentication worked at least with test host
        	return StatusImage.OK;
        } catch (HandleCreationException e) {
        	// Authentication failed with test host - could work with other hosts though
    	    return StatusImage.WARN;
        } catch (HandleOperationException e) {
        	// Closing handle failed with test host - could work with other hosts though
    	    return StatusImage.WARN;
        }
    }
*/
	private String getAuth(Realm realm) {
		return missingAuth(realm) ? "< MISSING >" : "***********";
    }

	private boolean missingAuth(Realm realm) {
	    return realm.getAccount().missingPasskey();
    }
}

