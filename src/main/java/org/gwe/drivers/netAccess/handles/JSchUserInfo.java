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

package org.gwe.drivers.netAccess.handles;

import org.gwe.utils.security.AccountInfo;

import com.jcraft.jsch.UserInfo;

/**
 * @author Marco Ruiz
 * @since Oct 26, 2008
 */
public class JSchUserInfo implements UserInfo {
    private AccountInfo account;

    public JSchUserInfo(AccountInfo accountInfo) {
        account = accountInfo;
    }
    
    public String getPassphrase() {
        return new String(account.getPassphrase());
    }

    public String getPassword() {
        return new String(account.getPassword());
    }

    public boolean promptPassword(String string) {
        return true;
    }

    public boolean promptPassphrase(String string) {
        return account.getPassphrase() != null;
    }

    public boolean promptYesNo(String string) {
        return true;
    }

    public void showMessage(String string) {
        // this area intentionally left blank.
    }
}