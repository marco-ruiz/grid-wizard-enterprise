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

package org.gwe.app.client.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.persistence.model.GridInfo;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.VarInfo;
import org.gwe.utils.cmd.OptionParser;
import org.gwe.utils.security.CredentialNotFoundException;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.xstream.AliasTransf;
import org.gwe.utils.xstream.XMLConfigFile;

/**
 * @author Marco Ruiz
 * @since Jan 28, 2008
 */
public class XMLClientConfigReader implements ClientConfigReader {

	private static Log log = LogFactory.getLog(XMLClientConfigReader.class);

	public static final String CONF_ARG_PREFIX = "-conf=";
	
	public static final String DEFAULT_GRID_FILENAME = "gwe-grid.xml";
	private static final String DEFAULT_ENC_TOKEN    = "grid wizard enterprise";

	private HeadResourceInfo headResource = null;
	private String daemonName = null;
	private String gridFN = DEFAULT_GRID_FILENAME;
	private String encryptionToken = DEFAULT_ENC_TOKEN;

	private GridInfo grid;
	private KeyStore keys = null;
	
	public XMLClientConfigReader(String confArg) throws CredentialNotFoundException, IOException {
		InstallationFiles cfgFiles = ClientConfig.getInstallFiles();

		// Format: -conf=[OPTIONAL_CLUSTER_NAME]@[OPTIONAL_CLUSTERS_FILENAME]:[OPTIONAL_KEYSTORE_FILENAME]
		OptionParser option = new OptionParser(CONF_ARG_PREFIX, confArg, '@', ':');
		daemonName      = option.getEle(0, daemonName);
		gridFN          = option.getEle(1, cfgFiles.getConfigFilePath(gridFN));
		encryptionToken = option.getEle(2, encryptionToken);
		
		grid = readGrid();
	}

	private GridInfo readGrid() throws FileNotFoundException {
		AliasTransf<GridInfo>         at4Grid    = new AliasTransf<GridInfo>        (GridInfo.class,         "grid",    "headResources");
		AliasTransf<HeadResourceInfo> at4HeadRes = new AliasTransf<HeadResourceInfo>(HeadResourceInfo.class, "cluster", "vars");
		AliasTransf<VarInfo>          at4Var     = new AliasTransf<VarInfo>         (VarInfo.class,          "p2elVar");
		
		GridInfo grid = new XMLConfigFile<GridInfo>(gridFN, at4Grid, at4HeadRes, at4Var).getModel();
		headResource = grid.getHeadResource(daemonName);
    	if (headResource == null && grid.getHeadResources().size() > 0) headResource = grid.getHeadResources().get(0);
	    return grid;
    }

	public GridInfo getGrid() {
    	return grid;
    }

	public KeyStore getKeys() {
		if (keys == null) {
	        try {
	    		ConsoleKeyStorePasskeysReader reader = new ConsoleKeyStorePasskeysReader(ClientConfig.getInstallFiles(), encryptionToken, false);
				keys = reader.readKeyStore();
				keys.init();
	        } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
	        }
		}
        return keys;
    }

	public HeadResourceInfo getHeadResource() {
    	return headResource;
    }

	public static void main(String[] args) throws CredentialNotFoundException, IOException {
		new XMLClientConfigReader("-conf=@test-gwe-grid.xml:test-gwe-auth.xml");
	}
}

