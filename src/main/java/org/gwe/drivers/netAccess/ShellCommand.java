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

package org.gwe.drivers.netAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.utils.IOUtils;

/**
 * @author Neil Jones
 * @author Marco Ruiz
 * @since Jul 11, 2007
 */
public class ShellCommand {

	private static Log log = LogFactory.getLog(ShellCommand.class);

	public static String moveFile(String source, String destination) {
        return "mv " + source + " " + destination;
    }

	public static String copyFile(String source, String destination) {
        return "cp -r -p " + source + " " + destination;
    }

	public static String delete(String dir) {
        return "rm -fdr " + dir;
    }

	public static String makeFilePathDir(String file) {
        return "mkdir -p " + IOUtils.getFilePath(file);
    }

	public static final int NON_INACTIVITY_TIMEOUT = -1;
	
	private static ExecutionChannelInspector CHANNEL_OPENED_INSPECTOR = new ExecutionChannelInspector() {
		public boolean isExecutionChannelOpened() { return false; }
	};
	
	public static String runLocally(String cmd) throws ConnectorException {
		return new ShellCommand(cmd).runLocally();
	}

	private String path;
	private String cmd;
    private List<String> args = new ArrayList<String>();
	private Map<String, String> env;
	private int inactivityTimeout = NON_INACTIVITY_TIMEOUT;
	private String exitToken = null;
	private OutputStream outputStream = null;
	
	public ShellCommand(String cmd) {
		this(cmd, null, null);
	}

	public ShellCommand(String cmd, String path, Map<String, String> env) {
		this.path = path;
		this.cmd = cmd;
		this.env = env;
	}

	public String getCmd() {
		return cmd;
	}

	public Map<String, String> getEnv() {
		return env;
	}

	public String getPath() {
		return path;
	}
	
	public void addArgument(String arg) {
		cmd += " " + arg;
	}

    public String toString() {
        return cmd;
    }

    /**
	 * Default implementation creates a unix style command (pretty standard). 
	 * If needed other type of command extend this class and override this method.
	 */
	public String getUnixStyleCmd() {
		StringBuffer envString = new StringBuffer();
		if (env != null)
			for (String key : env.keySet()) 
				envString.append(" " + key + "=" + env.get(key));
		
		String realCmd = (path != null) ? "cd " + path + " && " : " ";
        realCmd += envString.toString() + cmd;
        for (String arg : args) realCmd += " \"" + arg + "\"";
        
        return realCmd;
	}

	public int getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(int inactivityTimeout) {
		this.inactivityTimeout = inactivityTimeout;
	}

	public String getExitToken() {
		return exitToken;
	}

	public void setExitToken(String exitToken) {
		this.exitToken = exitToken;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public String runLocally() throws ConnectorException {
		long start = System.currentTimeMillis();
        if (cmd == null) 
        	throw new NullPointerException("Cannot execute 'null' shell command.");
        
        ProcessBuilder pb = new ProcessBuilder(cmd.split("\\s+"));
        if (getEnv() != null) pb.environment().putAll(getEnv());
        if (getPath() != null) pb.directory(new File(getPath()));

        try {
//        	Set<Thread> threads = (exitToken == null) ? null : Thread.getAllStackTraces().keySet();
            Process processObj = pb.start();
            log.debug("Command started '" + cmd + "'");
            String results = IOUtils.readStream(processObj.getInputStream(), getOutputStream(), getExitToken()).toString();
            log.debug("Command executed with results:\n" + results);
            int exitCode = (exitToken == null) ? processObj.waitFor() : 0; //exitedWithToken(threads);
        	return logResult(start, results, exitCode);
        } catch (ConnectorException ex) {
        	throw ex;
        } catch (Exception ex) {
            throw new ConnectorException("Command '" + cmd + "' execution failed", ex);
        }
    }
	
	// Hack to close those unnecessary process reapers that are locking daemon installations
	private int exitedWithToken(Set<Thread> threads) {
		Set<Thread> currentThreads = Thread.getAllStackTraces().keySet();
		for (Thread thread : currentThreads) {
	        String threadName = thread.getName();
			if (!threads.contains(thread) && threadName.contains("process reaper"))
				thread.stop();
        }
	    return 0;
    }

	private String logResult(long start, String results, int exitCode) throws ConnectorException {
	    String logMsgPrefix = "Command [duration=" + (System.currentTimeMillis() - start) + "ms] '" + cmd + "'";
	    if (exitCode != 0) {
	    	String msg = logMsgPrefix + " failed with exit value: " + exitCode;
	    	log.info(msg);
	    	throw new ConnectorException(msg + "\nOutput:\n\n" + results);
	    } else {
	        log.debug(logMsgPrefix + "' successfully executed!");
	    }
	    return results;
    }
	
	public String readExecutionOutput(InputStream in) throws IOException {
		return readExecutionOutput(in, null);
	}
	
	public String readExecutionOutput(InputStream in, ExecutionChannelInspector inspector) throws IOException {
		if (inspector == null) inspector = CHANNEL_OPENED_INSPECTOR;
		StringBuffer out = new StringBuffer();
		byte[] buff = new byte[1024];
		long startInactivity = System.currentTimeMillis();
		while (true) {
		    while (in.available() > 0) {
		    	// Read output
		        int count = in.read(buff);
		        log.trace("Read " + count + " bytes");
		        if (count < 0) break;
		        
		        // Save output
		        String readStr = new String(buff, 0, count);
				out.append(readStr);
		        if (outputStream != null) outputStream.write(readStr.getBytes());
		        
		        // Check if exit token reached
		        if (exitToken != null && out.lastIndexOf(exitToken) != -1) return out.toString();
		        
		        // Reset inactivity
			    startInactivity = System.currentTimeMillis();
		    }
		    if (reachedInactivityTimeout(startInactivity) || !inspector.isExecutionChannelOpened()) return out.toString();
		}
	}

	private boolean reachedInactivityTimeout(long startInactivity) {
	    return inactivityTimeout != NON_INACTIVITY_TIMEOUT && System.currentTimeMillis() > startInactivity + inactivityTimeout;
    }
}


