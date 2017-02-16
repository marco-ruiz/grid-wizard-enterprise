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

package org.gwe.drivers.fileSystems;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.config.XMLClientConfigReader;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.ResourceHandle;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.CredentialNotFoundException;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;

/**
 * @author Marco Ruiz
 * @since Jan 24, 2007
 */
public abstract class FileHandle extends ResourceHandle {
    private static Log log = LogFactory.getLog(FileHandle.class);

    public FileHandle(ResourceLink<FileHandle> link) { 
    	super(link);
    }

    public ThinURI getURI() { 
    	return link.getURI(); 
    }

    /**
     * @return is this a directory?
     * @throws HandleOperationException - system-level error.
     */
    public abstract boolean isDirectory() throws HandleOperationException;

    public abstract InputStream getInputStream() throws HandleOperationException;
    
    public abstract OutputStream getOutputStream() throws HandleOperationException;
    	
    public abstract void createFolder() throws HandleOperationException;

    public abstract boolean exists() throws HandleOperationException;
    
    public boolean delete() throws HandleOperationException {
    	throw new HandleOperationException("Operation Not Supported!");
    }
    
    public boolean isGlob() {
        return link.getURI().toString().matches(".*[\\*\\?\\[].*");
    }
    
    public <OBJECT_TYPE> OBJECT_TYPE readObject() throws HandleOperationException {
		try {
			ObjectInputStream ois = new ObjectInputStream(getInputStream());
			OBJECT_TYPE result = (OBJECT_TYPE) ois.readObject();
			ois.close();
			return result;
		} catch (IOException e) {
			throw new HandleOperationException("Could not read object from file", e);
		} catch (ClassNotFoundException e) {
			throw new HandleOperationException("Could not read object from file", e);
		}
    }
    
    public void storeObject(Object toSerialize) throws HandleOperationException {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(getOutputStream());
			oos.writeObject(toSerialize);
			oos.close();
		} catch (IOException e) {
			throw new HandleOperationException(e);
		}
    }
    
    public void copyTo(FileHandle otherFile) throws HandleOperationException {
    	if (!exists()) return; 
    	if (isDirectory()) {
    		copyToDir(otherFile);
    	} else {
	        log.trace("Copying to " + otherFile.getURI().getPath());
	        try {
	        	IOUtils.pipeStreams(getInputStream(), otherFile.getOutputStream(), true);
	        } catch (IOException ioex) {
	            throw new HandleOperationException("I/O error reading contents from file", ioex);
	        }
			log.trace("Finished file copy");
    	}
    }

	protected void copyToDir(FileHandle otherFile) throws HandleOperationException {
		// Create path to destination
		if (!otherFile.exists()) otherFile.createFolder(); 
		if (!otherFile.isDirectory()) 
			throw new HandleOperationException("Cannot copy a directory (" + this.getURI() + ") to a file destination (" + otherFile.getURI() + ")");
	    otherFile.delete();
	    
	    // Compress directory
	    FileHandle srcFileCompressed = createCompressedCopyHandle(true);
	    FileHandle otherFileCompressed = otherFile.createCompressedCopyHandle(false);
	    
	    // Copy compressed directory to temporary compressed file on destination path
	    srcFileCompressed.copyTo(otherFileCompressed);
	    srcFileCompressed.delete();
	    srcFileCompressed.close();

	    // Decompress bundled directory just copied
	    otherFileCompressed.decompressInto(otherFile, IOUtils.getFileName(getPath()));
	    otherFileCompressed.delete();
	    otherFileCompressed.close();
    }

	protected FileHandle createCompressedCopyHandle(boolean createFile) throws HandleOperationException {
    	throw new HandleOperationException("Cannot compress file " + getURI()); 
    }

    protected void decompressInto(FileHandle otherFile, String directoryName) throws HandleOperationException {
    	throw new HandleOperationException("Cannot decompress file " + getURI()); 
    }

	/**
     * The equivalent of "ls" in a directory.
     *
     * @return a list of files in the directory.
     * @throws HandleOperationException - system-level error.
     */
    public abstract FileHandle[] getChildren() throws HandleOperationException;

    /**
     * @return number of bytes in the file
     * @throws HandleOperationException - system-level error.
     */
    public abstract long getSize() throws HandleOperationException;
    
	public String getPath() {
		return getURI().getPath();
	}
	
	public static void main(String[] args) throws CredentialNotFoundException, HandleOperationException, IOException {
		ClientConfig appConfig = new ClientConfig(new XMLClientConfigReader(null));
//		FileHandle srcDir = GWEAppContext.getGridFileSystem().createHandle("http://www.gridwizardenterprise.org/guides/", appConfig.getKeys());
		FileHandle srcDir = appConfig.getKeys().createFileLink("sftp://birn-cluster0.nbirn.net/home/mruiz/63Test").createHandle();
		FileHandle destDir = appConfig.getKeys().createFileLink("sftp://www.gridwizard.org/home/mruiz/final").createHandle();
		srcDir.copyTo(destDir);
	}
}
