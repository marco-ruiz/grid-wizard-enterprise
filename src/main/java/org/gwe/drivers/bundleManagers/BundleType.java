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

package org.gwe.drivers.bundleManagers;


/**
 * @author Marco Ruiz
 * @since Aug 12, 2007
 */
public enum BundleType {
	NONE("ls", ""),
	ZIP("unzip", "zip"), 
	TAR("tar -xf", "tar"),
	TAR_GZ("tar -xzf", "tar.gz"),
	TAR_BZ2("tar -xjf", "tar.bz2");

	public static BundleType get(String ext) {
		if (ext != null) {
			ext = ext.toLowerCase();
			for (BundleType	type : BundleType.values())
		        if (type.getExtension().equals(ext)) return type;
		}
		
		return null;
	}
	
	private String cmd;
	private String ext;
	
	BundleType(String unbundleCommand, String extension) {
		cmd = unbundleCommand;
		ext = extension;
	}

	public String getUnbundleCommand() {
		return cmd;
	}

	public String getExtension() {
    	return ext;
    }

	public String createName(String name) {
		return name + (ext.equals("") ? "" : "." + ext);
	}
}
