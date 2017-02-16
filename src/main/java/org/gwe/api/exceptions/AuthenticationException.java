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

package org.gwe.api.exceptions;

/**
 * @author Marco Ruiz
 * @since Mar 5, 2008
 */
public class AuthenticationException extends GWEDomainException {
	
	public AuthenticationException() {
		super();
	}

	public AuthenticationException(String msg, Throwable e) {
		super(msg, e);
	}

	public AuthenticationException(String msg) {
		super(msg);
	}

	public AuthenticationException(Throwable e) {
		super(e);
	}

}
