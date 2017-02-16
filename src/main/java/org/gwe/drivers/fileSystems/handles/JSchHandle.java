/*
 * Copyright 2007-2008 the original author or authors. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.gwe.drivers.fileSystems.handles;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.HandleCreationException;
import org.gwe.drivers.HandleOperationException;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.netAccess.handles.JSchConnection;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.ResourceLink;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * @author Marco Ruiz
 * @since Aug 23, 2007
 */
public class JSchHandle extends FileHandle {
	
	private static Log log = LogFactory.getLog(JSchHandle.class);
	
	private JSchConnection conn;
	private ChannelSftp sftpChannel;
	private String fileName;
	
	public JSchHandle(ResourceLink<FileHandle> link) throws HandleCreationException {
		super(link);
		try {
			conn = new JSchConnection(link.cloneToHostLink(ProtocolScheme.SSH));
	        sftpChannel = createChannel();
		} catch (Exception ex) {
			log.info("Connection failed: aborting", ex);
			throw new HandleCreationException("Jsch unable to establish a connection.", ex);
		}
	}
	
	private ChannelSftp createChannel() throws HandleOperationException {
	    try {
    		Channel channel = conn.getSessionObj().openChannel("sftp");
			channel.connect();
			return (ChannelSftp)channel;
        } catch (JSchException e1) {
        	throw new HandleOperationException("");
        }
    }

	@Override
    public void createFolder() throws HandleOperationException {
    }
	
	@Override
	public boolean exists() throws HandleOperationException {
		InputStream in = System.in;
		PrintStream out = System.out;
				
		byte[] buf = new byte[1024];
		int i;
		String str;
		int level=0;

		try {
			sftpChannel.cd("");
			sftpChannel.lcd("");
		} catch(SftpException e){
			System.out.println(e.toString());
		}
		return true;
	}
	
	@Override
	public FileHandle[] getChildren() throws HandleOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public InputStream getInputStream() throws HandleOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public OutputStream getOutputStream() throws HandleOperationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public long getSize() throws HandleOperationException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean isDirectory() throws HandleOperationException {
		// TODO Auto-generated method stub
		return false;
	}
}

