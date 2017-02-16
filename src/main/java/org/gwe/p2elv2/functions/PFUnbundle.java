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

package org.gwe.p2elv2.functions;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.bundleManagers.BundleHandle;
import org.gwe.drivers.bundleManagers.BundleType;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.p2elv2.PFunctionSingleValue;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValue;

/**
 * @author Marco Ruiz
 * @since Aug 11, 2008
 */
public class PFUnbundle extends PFunctionSingleValue {

	private static Log log = LogFactory.getLog(PFUnbundle.class);
    private static final String ERROR_PATH = "UNIDENTIFIED_BUNDLE_TYPE";

	public PFUnbundle() { super("unbundle"); }
	
    public PVarValue calculateValue(List<String> params, PStatementContext ctx) {
    	BundleType bundleType = extractBundleType(params);
    	if (bundleType == null) return new PVarValue(ERROR_PATH);

		String localFile = params.get(0);
		BundleHandle bundle = new BundleHandle(bundleType, localFile);
    	try {
	        bundle.getUnbundleShellCommand().runLocally();
        } catch (ConnectorException e1) {
        }
        
        return new PVarValue(bundle.getPath());
    }

	private BundleType extractBundleType(List<String> params) {
    	if (params.size() == 1) {
    		String fileName = params.get(0);
    		for (BundleType bType : BundleType.values()) {
	            if (fileName.endsWith(bType.getExtension())) 
	            	return bType;
            }
    	} else {
    		return BundleType.valueOf(params.remove(0));
    	}
	    return null;
    }
}
