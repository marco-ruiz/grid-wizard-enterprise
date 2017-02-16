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

package org.gwe.drivers.fileSystems.handles;

import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.UserAuthenticator;
import org.gwe.utils.security.AccountInfo;

/**
 * @author Marco Ruiz
 * @since Dec 6, 2008
 */
class VfsAuthBridge implements UserAuthenticator {
    private String userName;
    private String password;

    VfsAuthBridge(AccountInfo account) {
        super();
        userName = account.getUser();
        password = account.getPassword();
    }

    public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] types) {
        UserAuthenticationData res = new UserAuthenticationData();
        for (int ii = 0; ii < types.length; ++ii) {
            if (types[ii].equals(UserAuthenticationData.USERNAME)) {
                res.setData(types[ii], userName.toCharArray());
            }
            else if (types[ii].equals(UserAuthenticationData.PASSWORD)) {
                res.setData(types[ii], (password != null) ? password.toCharArray() : null);
            }
            else {
                throw new UnsupportedOperationException("Domain-based authentication not supported.");
            }
        }
        return res;
    }
}