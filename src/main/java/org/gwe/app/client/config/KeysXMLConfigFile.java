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

import org.gwe.utils.security.AccessControl;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.Realm;
import org.gwe.utils.xstream.AliasTransf;
import org.gwe.utils.xstream.XMLConfigFile;

/**
 * @author Marco Ruiz
 * @since Jan 15, 2009
 */
public class KeysXMLConfigFile extends XMLConfigFile<KeyStore> {

    private static AliasTransf<KeyStore>      at4Keys    = new AliasTransf<KeyStore>      (KeyStore.class,      "keystore", "accessControls");
    private static AliasTransf<AccessControl> at4ACs     = new AliasTransf<AccessControl> (AccessControl.class, "accessControl").setAttributize(false);
    private static AliasTransf<AccountInfo>   at4Account = new AliasTransf<AccountInfo>   (AccountInfo.class,   "account");
    private static AliasTransf<Realm>         at4Realms  = new AliasTransf<Realm>         (Realm.class,         "realm");
	
	public KeysXMLConfigFile(String fileName) throws FileNotFoundException {
	    super(fileName, at4Keys, at4ACs, at4Account, at4Realms);
    }
}
