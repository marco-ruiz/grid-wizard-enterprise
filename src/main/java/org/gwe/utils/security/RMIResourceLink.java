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

package org.gwe.utils.security;

import org.gwe.drivers.ResourceHandle;

/**
 * @author Marco Ruiz
 * @since Dec 8, 2008
 */
public class RMIResourceLink<HANDLE_TYPE extends ResourceHandle> extends ResourceLink<HANDLE_TYPE> {

	public RMIResourceLink(ThinURI uri, AccountInfo account) {
	    super(uri, account);
	    
    }
	
}
