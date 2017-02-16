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


/**
 * 
 * @author Marco Ruiz
 * @since Aug 1, 2007
 */
public class JargonHandle {} 

/*
extends FileSystemDriver<JargonHandle> {
    public static final String DOMAIN = "DOMAIN";
	public static final String DEFAULT_STORAGE_RESOURCE = "DEFAULT STORAGE RESOURCE";

	private static Log log = LogFactory.getLog(JargonDriver.class);

    public JargonDriver() throws HandleOperationException {}

	public JargonHandle tryToCreateHandle(URILink link) throws HandleCreationException {
		checkSchemeIsSupported(link, "srb");
		URI uri = link.getURI();
        AccountInfo acctInfo = link.getAccountInfo();
		SRBAccount account = new SRBAccount(uri.getHost(), uri.getPort(), 
        		acctInfo.getUserName(), "", acctInfo.getHomeDir(), acctInfo.getProperty(DOMAIN), 
        		acctInfo.getProperty(DEFAULT_STORAGE_RESOURCE));
        account.setOptions(SRBAccount.GSI_AUTH);
        log.info("Setting credential " + acctInfo.getAccountName() + " for file '" + uri + "'");
        account.setGSSCredential(acctInfo.getCredential());
        SRBFileSystem srbFS;
		try {
			srbFS = new SRBFileSystem(account);
	        SRBFile srbFile = new SRBFile(srbFS, uri.getPath());
	        return new JargonHandle(link, srbFile);
		} catch (Exception e) {
            throw new HandleCreationException(e);
		}
    }
}

class JargonHandle extends FileHandle {
    private GeneralFile fileObj;
    
    public JargonHandle(URILink link, GeneralFile generalFile) {
    	super(link);
    	fileObj = generalFile;
    }

    public boolean exists() throws HandleOperationException {
    	return fileObj.exists();
    }
    
    public boolean isDirectory() throws HandleOperationException {
        return fileObj.isDirectory();
    }

    public void createFile() throws HandleOperationException {
        if (exists()) return;

		try {
			fileObj.createNewFile();
		} catch (IOException e) {
            throw new HandleOperationException("Could not create new file '" + fileObj.getName() + "'", e);
		}
    }

    public void createFolder() throws HandleOperationException {
        if (exists()) return;
       	fileObj.mkdirs();
    }

    public InputStream getInputStream() throws HandleOperationException {
    	try {
			return FileFactory.newFileInputStream(fileObj);
		} catch (IOException e) {
            throw new HandleOperationException("Could not get file input stream for file '" + fileObj.getName() + "'", e);
		}
    }
    
    public OutputStream getOutputStream() throws HandleOperationException {
        try {
			return FileFactory.newFileOutputStream(fileObj);
		} catch (IOException e) {
            throw new HandleOperationException("Could not get file output stream for file '" + fileObj.getName() + "'", e);
        }
    }

    public long getSize() throws HandleOperationException {
        return (isDirectory()) ? -1 : fileObj.length();
    }

    public FileHandle[] getChildren() throws HandleOperationException {
        GeneralFile[] files = fileObj.listFiles();
        FileHandle[] res = new FileHandle[files.length];
        for (int idx = 0; idx < res.length; ++idx) {
			URILink currLink = new URILink(link.getAccountInfo(), files[idx].toURI());
			res[idx] = new JargonHandle(currLink, files[idx]);
		}
        return res;
    }
}
*/