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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.HandleOperationNotSupportedException;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

import sun.misc.BASE64Encoder;

/**
 * @author Marco Ruiz
 * @since May 25, 2008
 */
public class HTTPHandle extends FileHandle {
    private static Log log = LogFactory.getLog(HTTPHandle.class);
    
    public HTTPHandle(ResourceLink<FileHandle> link) {
    	super(link);
    }

    public void createFolder() throws HandleOperationException {
    	throw new HandleOperationNotSupportedException();
    }

    public boolean exists() throws HandleOperationException {
    	return true;
    }

    public FileHandle[] getChildren() throws HandleOperationException {
    	throw new HandleOperationNotSupportedException();
    }

    public InputStream getInputStream() throws HandleOperationException {
        try {
            URLConnection conn = link.getURI().toURI().toURL().openConnection();
            applyAuth(conn);                 
			return conn.getInputStream();
        } catch (MalformedURLException e) {
        	throw new HandleOperationException("Could not parsed the http URL '" + link.getURI() + "'", e);
        } catch (IOException e) {
        	throw new HandleOperationException(e);
        }
    }

	private void applyAuth(URLConnection conn) {
	    AccountInfo acct = link.getAccountInfo();
	    if (acct != null) {
		    String auth = acct.getUser() + ":" + acct.getPassword();
		    String encoded = new BASE64Encoder().encode(auth.getBytes());
		    conn.setRequestProperty("Authorization", "Basic " + encoded);
	    }
    }

    public OutputStream getOutputStream() throws HandleOperationException {
    	throw new HandleOperationNotSupportedException();
    }

    public long getSize() throws HandleOperationException {
	    return 0;
    }

    public boolean isDirectory() throws HandleOperationException {
    	return false;
    }
    
    public static void main(String[] args) {
    	try {
    		ThinURI uri = ThinURI.create("http://central.xnat.org/REST/projects?format=xml");
	    	ResourceLink<FileHandle> theLink = new ResourceLink<FileHandle>(uri, new AccountInfo("mruiz", "xnatmruiz"));
	    	IOUtils.pipeStreams(new HTTPHandle(theLink).getInputStream(), System.out);
    	} catch (Exception e) {}
    }
}

