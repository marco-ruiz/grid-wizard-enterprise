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

package org.gwe.persistence.model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.app.daemon.DaemonApp;
import org.gwe.drivers.fileSystems.FileHandle;
import org.gwe.drivers.netAccess.ShellCommand;
import org.gwe.utils.IOUtils;
import org.gwe.utils.security.AccountInfo;
import org.gwe.utils.security.KeyStore;
import org.gwe.utils.security.ProtocolScheme;
import org.gwe.utils.security.Realm;
import org.gwe.utils.security.ResourceLink;
import org.gwe.utils.security.ThinURI;
import org.hibernate.annotations.GenericGenerator;

/**
 * 
 * @author Marco Ruiz
 * @since Aug 15, 2007
 */
@GenericGenerator(name = "headResourceInfoIdGenerator", strategy = "org.gwe.persistence.model.HeadResourceInfoIdGenerator")
@Entity
public class HeadResourceInfo extends BaseModelInfo<String> {

	private static Log log = LogFactory.getLog(HeadResourceInfo.class);

	// Constants related to the OS shell script launching this application
    public static final String BASE_SCRIPT = "gwed-base-script.sh";
	
 	public static ShellCommand createDaemonScriptCommand(String daemonHomePath, Class<?> mainClass, int debugPort) {
		String binFolder  = OSAppFolder.BINARIES.getRelativeTo(daemonHomePath);
		String baseScript = IOUtils.concatenatePaths(binFolder, BASE_SCRIPT);
		String debugFlag  = "";
		if (debugPort >= 1000) debugFlag = debugPort + "";
		String launchCmd  = baseScript + " " + debugFlag + " " + mainClass.getName() + " " + daemonHomePath;
		
		// Command
		ShellCommand cmd = new ShellCommand(launchCmd, daemonHomePath, null);
        cmd.setInactivityTimeout(120000);
        return cmd;
	}

 	@Id
	@GeneratedValue(generator = "headResourceInfoIdGenerator")
    private String location = null;

	private String host;

	private String name = "";

	// Allocation policy
	private int queueSize = -1;			// No maximum limit on concurrent live allocations by default
	private float maxHijackMins = -1; 	// No disposing compute resource because it is too old 
	private float maxIdleMins = -1;	    // No disposing compute resource because it is too idle
	private float maxWaitMins = -1;     // No disposing compute resource because it is too late
	private long heartBeatPeriodSecs;
	
	private String version;
	private String resourceManager;
	
	private String installRootPath = "";
	private String databaseRootPath;
	
	private String platform;
	
    @OneToMany(cascade = CascadeType.ALL)
	private List<VarInfo> vars;

	@Transient
	private int debugPort = -1;
	
	@Transient
	private transient List<OrderInfo> ordersList = new ArrayList<OrderInfo>();
	
	public HeadResourceInfo() {}

	public HeadResourceInfo(String host, String daemonRootPath) {
		setHost(host);
		setInstallRootPath(daemonRootPath);
    }

	public String getId() { 
		return getLocation(); 
	}

	public String getLocation() {
		return (location == null) ? generateRMIBaseURI() : location;
	}
	
	private void setLocation(String location) {
		this.location = location;
	}
	
	public String getHost() {
		return host;
	}

	private void setHost(String host) {
		this.host = host;
	}

	public String getVersion() {
    	return version;
    }

	public void setVersion(String version) {
    	this.version = version;
    }

	public String getResourceManager() {
    	return resourceManager;
    }

	public void setResourceManager(String resourceManagers) {
    	this.resourceManager = resourceManagers;
    }

	public String getDatabasePath() {
    	return getGWEPath(getDatabaseRootPath());
    }

	public String getDatabaseRootPath() {
		if (databaseRootPath == null || "".equals(databaseRootPath)) 
			databaseRootPath = getInstallRootPath();
		
    	return databaseRootPath;
    }

	public void setDatabaseRootPath(String dbRootPath) {
    	this.databaseRootPath = dbRootPath;
    }

	public String getInstallRootPath() {
    	return installRootPath;
    }

	public void setInstallRootPath(String installRootPath) {
    	this.installRootPath = installRootPath;
    }

	public String getInstallPath() {
    	return getGWEPath(getInstallRootPath());
    }

	private String getGWEPath(String rootPath) {
    	return IOUtils.concatenatePaths(rootPath, "gwe-daemon", "gwe-" + version);
    }

	public DaemonInstallation getInstallation() {
    	return new DaemonInstallation(getInstallPath());
    }
	
	public String toFileProtocol(String filePath) {
		return (this.getAccessScheme().equals(ProtocolScheme.SSH)) ? 
				ProtocolScheme.SFTP.toURIStr(host, filePath) : ProtocolScheme.FILE.toURIStr("", filePath);
	}

	public int getRegistryPort(AccountInfo acct) {
        try {
			ThinURI uri = ThinURI.create(toFileProtocol(getInstallation().getPortFilePath()));
	    	InputStream fis = new ResourceLink<FileHandle>(uri, acct).createHandle().getInputStream();
	    	byte[] portBytes = new byte[8];
	    	fis.read(portBytes);
			return Integer.parseInt(new String(portBytes).trim());
        } catch (Exception e) {
        	return DaemonApp.DEFAULT_RMI_REGISTRY_PORT;
        }
	}
	
	public void setRegistryPort(int port) {
        try {
        	FileOutputStream fos = new FileOutputStream(getInstallation().getPortFilePath(), false);
        	byte[] portBytes = Integer.toString(port).getBytes(); 
	    	fos.write(portBytes);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
	}
	
	public String getName() {
		if ((name == null || "".equals(name)) && location != null) 
			name = location;
		return name;
	}

	public void setName(String name) {
		if (name != null) this.name = name;
	}
	
	public String getCompURI() {
		return getAccessScheme().toURIStr(host);
	}

	public ProtocolScheme getAccessScheme() {
	    return (!host.equals("localhost")) ? ProtocolScheme.SSH : ProtocolScheme.LOCAL;
    }
	
	public String getConnectionURL(KeyStore keys) {
		Realm realm = resolveRealm(keys);
		return (getHost() == null || realm == null) ? "" : realm.getAccount().getUser() + "@" + getHost(); 
	}

	private Realm resolveRealm(KeyStore keys) {
	    return keys.resolveRealm(getAccessScheme(), getHost());
    }

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	
	//==========
	// TIMEOUTS
	//==========
	public float getMaxHijackMins() {
		return maxHijackMins;
	}

	public long getMaxHijackMillis() {
		return (long)(maxHijackMins * 60000);
	}

	public void setMaxHijackMins(float value) {
		this.maxHijackMins = value;
	}
	
	public float getMaxIdleMins() {
		return maxIdleMins;
	}

	public long getMaxIdleMillis() {
		return (long)(maxIdleMins * 60000);
	}

	public void setMaxIdleMins(float value) {
		this.maxIdleMins = value;
	}

	public float getMaxWaitMins() {
	    return maxWaitMins;
    }

	public long getMaxAttachMillis() {
	    return (long)(maxWaitMins * 60000);
    }

	public void setMaxWaitMins(float maxAttachMins) {
	    this.maxWaitMins = maxAttachMins;
    }

	public long getHeartBeatPeriodSecs() {
		return heartBeatPeriodSecs;
	}

	public void setHeartBeatPeriodSecs(long value) {
		this.heartBeatPeriodSecs = value;
	}

	//=============
	// OTHER PROPS
	//=============
	public PlatformType getPlatform() {
    	return PlatformType.getByName(platform);
    }

	public void setPlatform(PlatformType platform) {
    	this.platform = platform.name();
    }

	public String getVarValue(String varName) {
		for (VarInfo var : vars) 
	        if (var.getName().equals(varName)) 
	        	return var.getValue();
	        
		return null;
	}
	
	public List<VarInfo> getVars() {
    	return vars;
    }

	public Map<String, Object> getVarsAsMap() {
		Map<String, Object> result = new HashMap<String, Object>(); 
		for (VarInfo var : vars) result.put(var.getName(), var.getValue());
    	return result;
    }

	public void setVars(List<VarInfo> vars) {
    	this.vars = vars;
    }

	public void setDebugPort(int debugPort) {
    	this.debugPort = debugPort;
    }

	public List<OrderInfo> getOrdersList() {
		if (ordersList == null)
			ordersList = new ArrayList<OrderInfo>();
    	return ordersList;
    }

	public void setOrdersList(List<OrderInfo> ordersList) {
    	this.ordersList = ordersList;
    }

	@Override
	public String toString() {
		return name + "->" + getLocation();
	}

	//=====================
	// COMPUTED PROPERTIES
	//=====================
	public String generateRMIBaseURI() {
		return ProtocolScheme.RMI.toURIStr(getHost(), generateRMIPath());
	}
	
	public String generateRMIPath() {
		return IOUtils.concatenatePaths(getInstallPath(), "DAEMON");
	}
	
	public ShellCommand createDaemonLauncherCommand() {
        ShellCommand cmd = createDaemonScriptCommand(getInstallPath(), DaemonApp.class, debugPort);
        cmd.setExitToken(DaemonApp.DAEMON_APP_MAIN_COMPLETED_MSG);
        return cmd;
    }

	@Override
	public int hashCode() {
		return getLocation().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final HeadResourceInfo other = (HeadResourceInfo) obj;
		return (getLocation().equals(other.getLocation()));
	}
}

