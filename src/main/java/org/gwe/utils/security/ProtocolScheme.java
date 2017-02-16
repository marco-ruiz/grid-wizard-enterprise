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

import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Sep 17, 2008
 */
public enum ProtocolScheme {
	
	FILE("file"),
	SFTP("sftp"), 
	HTTP("http"), 
	LOCAL("local"),
	SSH("ssh"),
	RMI("rmi");

	private String scheme;

	ProtocolScheme(String scheme) {
		this.scheme = scheme;
	}
	
	public String toURIStr(String... suffixes) {
		return scheme + ThinURI.SCHEME_SEPARATOR + IOUtils.concatenatePaths(suffixes);
	}

	public String toString() {
		return scheme;
	}
}
