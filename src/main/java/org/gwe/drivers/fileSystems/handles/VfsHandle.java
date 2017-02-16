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
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpPPKAuth;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.HostHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.drivers.netAccess.handles.JSchUserInfo;
import org.gwe.utils.IOUtils;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

/**
 * This class wraps a VFS filesystem.
 * 
 * @author Neil Jones
 * @author Marco Ruiz
 * @since Jan 24, 2007
 */
public class VfsHandle extends FileHandle {
    private static Log log = LogFactory.getLog(VfsHandle.class);
    
    private static FileSystemManager vfsManager = null;

    private FileObject fileObj;

    public VfsHandle(ResourceLink<FileHandle> link) throws FileSystemException, HandleOperationException {
    	super(link);
        String uriStr = link.getURI().toString();
        fileObj = (link.getAccountInfo() != null) ? 
        		getVFSManager().resolveFile(uriStr, createOptions(link)) : getVFSManager().resolveFile(uriStr);
    }
    
    private VfsHandle(ResourceLink<FileHandle> link, FileObject fileObj) throws FileSystemException, HandleOperationException {
    	super(link);
        this.fileObj = fileObj; 
    }
    
    private FileSystemManager getVFSManager() throws HandleOperationException {
        // TODO: For some reason, the SFTP provider deadlocks.  This needs fixing. 
        try {
            if (vfsManager == null) vfsManager = VFS.getManager();
            return vfsManager;
//            ((StandardFileSystemManager)vfsManager).addProvider("ssftp", provider);
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Unable to connect to the manager; this is probably fatal", fex);
        }
    }

	private FileSystemOptions createOptions(ResourceLink<FileHandle> link) throws FileSystemException {
        log.debug("Setting credential \"" + link + "\" for file '" + link.getURI().getPath() + "'");
        AccountInfo acct = link.getAccountInfo();
	    FileSystemOptions opts = new FileSystemOptions();
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, new VfsAuthBridge(acct));
	    if (ProtocolScheme.SFTP.toString().equalsIgnoreCase(link.getURI().getScheme())) {
	    	SftpFileSystemConfigBuilder configBuilder = SftpFileSystemConfigBuilder.getInstance();
	    	configBuilder.setStrictHostKeyChecking(opts, "ask");
	    	configBuilder.setUserInfo(opts, new JSchUserInfo(link.getAccountInfo()));
	    	if (acct.getPassword() == null) {
		        SftpPPKAuth ppkAuth = new SftpPPKAuth(acct.getPrivateKey(), acct.getPublicKey(), acct.getPassphrase());
				configBuilder.setPPKAuth(opts, ppkAuth);
		    }
	    }
	    return opts;
    }

	public void createFolder() throws HandleOperationException {
        if (exists()) return;

        try {
            fileObj.createFolder();
        } catch(FileSystemException e) {
            throw new HandleOperationException("Error creating folder", e);
        }
    }

    public boolean exists() throws HandleOperationException {
        try {
            return fileObj.exists();
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Could not check file existence", fex);
        }
    }
    
    public boolean isDirectory() throws HandleOperationException {
        try {
            return fileObj.getType().hasChildren();
        } catch (NullPointerException e) {
        	return false;
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Could not check directoriness", fex);
        }
    }

    public void copyTo(FileHandle otherFile) throws HandleOperationException {
    	super.copyTo(otherFile);
    }

	@Override
    protected FileHandle createCompressedCopyHandle(boolean createFile) throws HandleOperationException {
		String filePath = IOUtils.getFilePath(getPath());
        String fileName = IOUtils.getFileName(getPath());
        String newFileName = fileName + "-gwe-" + UUID.randomUUID().toString() + "-gwe.tar.gz";
        
        if (createFile) {
			try {
				HostHandle sshHandle = createHostHandle();
				sshHandle.runCommand(new ShellCommand("tar -czf " + newFileName + " " + fileName, filePath, null));
		        sshHandle.close();
	        } catch (ConnectorException e) {
	        	throw new HandleOperationException(e);
	        } catch (REXException e) {
	        	throw new HandleOperationException(e);
            }
        }
		
        String newFileURI = IOUtils.concatenatePaths(IOUtils.getFilePath(link.getURI().toString()), newFileName);
        try {
            ThinURI fileURI = ThinURI.create(ThinURI.asNormalizedFileURI(link.getURIHost(), newFileURI));
			ResourceLink<FileHandle> fileLink = new ResourceLink<FileHandle>(fileURI, link.getAccountInfo());
	        return fileLink.createHandle();
        } catch (REXException e) {
        	throw new HandleOperationException(e);
        }
    }
	
	@Override
    protected void decompressInto(FileHandle otherFile, String directoryName) throws HandleOperationException {
        String compressedFileName = IOUtils.getFileName(getPath());
        String compressedFilePath = IOUtils.getFilePath(getPath());
        String extractedDirFullPath = IOUtils.concatenatePaths(compressedFilePath, directoryName);

		try {
			HostHandle sshHandle = createHostHandle();
	        sshHandle.runCommand(new ShellCommand("tar -xzf " + compressedFileName, compressedFilePath, null));
			sshHandle.runCommand("mv " + extractedDirFullPath + " " + otherFile.getPath());
	        sshHandle.close();
        } catch (ConnectorException e) {
        	throw new HandleOperationException(e);
        } catch (REXException e) {
        	throw new HandleOperationException(e);
        }
    }

	public InputStream getInputStream() throws HandleOperationException {
        try {
            return fileObj.getContent().getInputStream();
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Could not read contents from file '" + link.getURI().getPath() + "'", fex);
        }
    }
    
    public OutputStream getOutputStream() throws HandleOperationException {
        try {
            return fileObj.getContent().getOutputStream();
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Could not create contents for file '" + link.getURI().getPath() + "'", fex);
        }
    }
    
    public FileHandle[] getChildren() throws HandleOperationException {
        try {
            FileObject[] files = fileObj.getChildren();
            FileHandle[] res = new FileHandle[files.length];
            for (int idx = 0; idx < res.length; ++idx) {
				ThinURI uri = new ThinURI(getURI().getScheme(), getURI().getHost(), files[idx].getName().getPath());
				ResourceLink currLink = new ResourceLink(uri, link.getAccountInfo());
				res[idx] = new VfsHandle(currLink, files[idx]);
			}
            return res;
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Error listing directory", fex);
        }
    }

    public long getSize() throws HandleOperationException {
        try {
            return fileObj.getContent().getSize();
        } catch (FileSystemException fex) {
            throw new HandleOperationException("Could not compute file size", fex);
        }
    }

	public boolean delete() throws HandleOperationException {
		try {
			fileObj.delete(new AllFileSelector());
			return fileObj.delete();
		} catch (FileSystemException fex) {
            throw new HandleOperationException("Could not delete file", fex);
		}
	}
	
	public void close() throws HandleOperationException {
		try {
	        fileObj.close();
        } catch (IOException e) {
        	throw new HandleOperationException("Could not close file", e);
        }
	}
	
	public void finalize() {
		if (fileObj != null) {
			try {
	            close();
            } catch (HandleOperationException e) {}
		}
	}
}



