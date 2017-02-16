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

package org.gwe.persistence.model.order;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.drivers.netAccess.ConnectorException;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.IOUtils;

/**
 * @author Marco Ruiz
 * @since Sep 27, 2007
 */
public class OSCommandDaemonRequest<DRP_TYPE extends Serializable> extends DaemonRequest<DRP_TYPE> {

	private static Log log = LogFactory.getLog(OSCommandDaemonRequest.class);

	private static String EXIT_CODE_TOKEN = "EXIT_CODE=";
	private static String EXIT_CODE_COMMAND = "\necho '" + EXIT_CODE_TOKEN + "'$?";
	
	protected String localizedCmd;
	protected String workspacePath;

	public void setOSCommand(String localizedCmd) {
		this.localizedCmd = localizedCmd;
	}

	public void setWorkspacePath(String workspacePath) {
		this.workspacePath = workspacePath;
	}

	public final Serializable process(String allocWorkspace) throws Exception {
		setup();
		String result = IOUtils.concatenatePaths(allocWorkspace, "result-" + execId + ".out");
		FileOutputStream fos = null;
		try {
			log.info("Preparing daemon request execution.\n\tLocalized command: '" + localizedCmd + "' \n\tWorkspace: '" + workspacePath + "'");
			if (localizedCmd != null && !localizedCmd.equals("")) {
				// Create wrapper script
				String wrapperScript = createFile(allocWorkspace, "job-" + execId + ".sh", "#!/bin/sh\n" + localizedCmd + "\n");
				pause(1000);
	
				// Execute wrapper script 
				ShellCommand cmd = new ShellCommand(wrapperScript, workspacePath, null);
				String outResult = cmd.runLocally();
				
				// Parse results and exit code
/*
				int lastIndex = outResult.lastIndexOf(EXIT_CODE_TOKEN);
				String exitCode = outResult.substring(lastIndex + EXIT_CODE_TOKEN.length());
				if (!"0".equals(exitCode)) {
					
				}
*/
				// Save results
				fos = writeResultOutputFile(result, outResult.getBytes());
			}
			return result;
		} catch(ConnectorException e) {
			fos = writeResultOutputFile(result, e.getMessage().getBytes());
			e.setCommand(localizedCmd);
			throw e;
		} catch(Exception e) {
			fos = writeResultOutputFile(result, e.getMessage().getBytes());
			throw e;
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					log.warn("Result file could not be closed", e);
				}
			tearDown();
		}
	}

	private FileOutputStream writeResultOutputFile(String result, byte[] bytes) throws FileNotFoundException, IOException {
	    FileOutputStream fos = new FileOutputStream(result, false);
	    fos.write(bytes);
	    return fos;
    }
	
	private void pause(long millis) {
		try {
	        Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
		Thread.yield();
	}
	
	public String toString() {
		return localizedCmd;
	}

	protected Map<String, String> getEnvironment() { return new HashMap<String, String>(); }

	protected void setup() {}

	protected void tearDown() {}
	
	protected String createFileUnderWorkspace(String fileName, String contents) {
		return createFile(workspacePath, fileName, contents);
	}

	protected String createFile(String root, String fileName, String contents) {
		return createFile(IOUtils.concatenatePaths(root, fileName), contents);
	}

	protected String createFile(String fullFileName, String contents) {
        try {
        	return IOUtils.createLocalExecutableFile(fullFileName, contents);
        } catch (IOException e) {
			// TODO: throw a real exception
        	log.warn("Couldn't create file '" + fullFileName + "'", e);
			throw new RuntimeException("Exception thrown when agent tried to create a file for a job. Please contact developers", e);
        }
	}
	
	public static void main(String[] args) throws IOException {
		OSCommandDaemonRequest<Serializable> request = new OSCommandDaemonRequest<Serializable>();
		String path = "/Users/admin/temp/";
		request.setWorkspacePath(path);
		String cmd = "ps aux | grep admin";
		String wrapperScript = request.createFileUnderWorkspace("process4job-1.sh", "#!/bin/sh\n" + cmd + "\n");

		ProcessBuilder pb = new ProcessBuilder(wrapperScript);
        pb.directory(new File(path));
        Process proc = pb.start();
		FileOutputStream fos = new FileOutputStream(IOUtils.concatenatePaths(path, "result.txt"), false);
        String results = IOUtils.readStream(proc.getInputStream(), fos).toString();
        System.out.print(results);
	}
}
