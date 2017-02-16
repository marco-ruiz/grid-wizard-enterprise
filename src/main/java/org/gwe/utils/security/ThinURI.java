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

import java.net.URI;

import org.gwe.utils.IOUtils;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.rex.REXParser;
import org.gwe.utils.rex.config.REXConfig4Class;
import org.gwe.utils.rex.config.REXConfig4Field;
import org.gwe.utils.rex.config.REXConfig4String;

/**
 * @author Marco Ruiz
 * @since May 15, 2008
 */
@REXConfig4Class(rexPieces={"scheme", ThinURI.SCHEME_SEPARATOR, "host", IOUtils.FILE_SEPARATOR, "path"})
public class ThinURI {
	
	public static final String SCHEME_SEPARATOR = "://";

	public static ThinURI createBlind(String uri) {
        try {
	        return create(uri);
        } catch (REXException e) {
        	return null;
        }
    }
	
	public static ThinURI create(String uri) throws REXException {
		if (!uri.contains(SCHEME_SEPARATOR)) uri += ProtocolScheme.FILE.toURIStr(uri);
        return REXParser.createModel(ThinURI.class, uri);
    }
	
	public static String asNormalizedFileURI(String hostName, String file) {
		// Add default local file system scheme if missing an scheme
		if (!file.contains(SCHEME_SEPARATOR))
			file = ProtocolScheme.FILE.toURIStr(file);
		
		// Transform to SFTP if 'hostName' is not null and file is a local file 
		if (hostName != null && !hostName.equals("") && !hostName.equals("localhost") && !hostName.equals("127.0.0.1"))
			file = file.replace(ProtocolScheme.FILE.toURIStr(), ProtocolScheme.SFTP.toURIStr(hostName));
		
		return file;
	}
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[^:]*"))
	private String scheme;
	
	@REXConfig4String(pattern=@REXConfig4Field(field="[^/]*"))
	private String host;
	
	@REXConfig4String(pattern=@REXConfig4Field(field=".*"))
	private String path;
	
	private String toString = null; 

	public ThinURI() {}

	public ThinURI(ProtocolScheme scheme, String location, String path) {
	    this(scheme.toString(), location, path);
    }

	public ThinURI(String scheme, String location, String path) {
	    this.scheme = scheme;
	    this.host = location;
	    setPath(path);
    }

	public String getScheme() {
    	return scheme;
    }

	public ProtocolScheme getProtocolScheme() {
    	return ProtocolScheme.valueOf(scheme.toUpperCase());
    }

	public void setScheme(String scheme) {
    	this.scheme = scheme;
    }

	public String getHost() {
    	return host;
    }

	public void setHost(String location) {
    	this.host = location;
    }

	public String getPath() {
    	return path;
    }

	public void setPath(String path) {
		if (path == null) path = ""; 
    	this.path = IOUtils.concatenatePaths("", path);
    }

	public String toString() {
		if (toString == null) 
			toString = scheme.toString() + SCHEME_SEPARATOR + IOUtils.concatenatePaths(host, path.toString());
		return toString;
	}
	
	public URI toURI() {
		return URI.create(toString());
	}
	
	public ThinURI clone() {
		return new ThinURI(scheme, host, path);
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((host == null) ? 0 : host.hashCode());
	    result = prime * result + ((path == null) ? 0 : path.hashCode());
	    result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    ThinURI other = (ThinURI) obj;
	    if (host == null) {
		    if (other.host != null)
			    return false;
	    } else if (!host.equals(other.host))
		    return false;
	    if (path == null) {
		    if (other.path != null)
			    return false;
	    } else if (!path.equals(other.path))
		    return false;
	    if (scheme == null) {
		    if (other.scheme != null)
			    return false;
	    } else if (!scheme.equals(other.scheme))
		    return false;
	    return true;
    }

	public static void main(String[] args) throws Exception {
		String uriStr = ProtocolScheme.SFTP.toURIStr("birn-cluster0.nbirn.net", "/~/path/to/file");
		ThinURI uri1 = ThinURI.create(uriStr);
		ProtocolScheme protScheme = uri1.getProtocolScheme();
		System.out.println(uri1);
		System.out.println(ThinURI.create("ssh://localhost/"));
	}
}

