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

package org.gwe.app.daemon.domain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.gwe.GWEAppContext;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.DaemonInstallation;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.utils.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Marco Ruiz
 * @since Feb 24, 2009
 */
public class ResultsPublisher {

	private DaemonConfigDesc config;
	
	public ResultsPublisher(DaemonConfigDesc config) {
	    this.config = config;
    }

	public void publishOrder(OrderInfo order, String publisherURI) {
		try {
	        publish(order, publisherURI);
        } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
	
	private void publish(OrderInfo order, String publisherURI) throws Exception {
		String wsPath = config.getHeadResource().getInstallation().getWorkspacePath();
		String destURI   = IOUtils.concatenatePaths(publisherURI, UUID.randomUUID().toString());
		String sourceDir = serializeOrder(order, wsPath);
		GWEAppContext.getGridFileSystem().uploadFile(sourceDir, destURI, config.getKeys());
	}

	private String serializeOrder(OrderInfo order, String wsPath) throws IOException {
	    String sourceDir = IOUtils.concatenatePaths(wsPath, "tmp", "results", order.getUniversalId());
		DaemonInstallation.createIfNonExistent(sourceDir);
        XStream result = new XStream(new DomDriver());
		OutputStream fos = new FileOutputStream(IOUtils.concatenatePaths(sourceDir, "order.xml"));
        result.toXML(order, fos);
	    return sourceDir;
    }
	
	public static void main(String[] args) throws IOException {
		ResultsPublisher publisher = new ResultsPublisher(null);
		OrderInfo order = new OrderInfo();
		order.stampWhenCreated();
		System.out.println(publisher.serializeOrder(order, "/Users/admin/gwe-daemon/test-workspace"));
	}
}

