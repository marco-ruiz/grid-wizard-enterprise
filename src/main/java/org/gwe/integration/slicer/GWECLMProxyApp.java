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

package org.gwe.integration.slicer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gwe.GWEAppContext;
import org.gwe.api.ServerAPIConnectionException;
import org.gwe.api.Session4ClientAPIEnhancer;
import org.gwe.api.event.DefaultEventFilter;
import org.gwe.api.exceptions.GWEDomainException;
import org.gwe.api.exceptions.PasswordMismatchException;
import org.gwe.app.client.ProgressTracker;
import org.gwe.app.client.admin.InstallerException;
import org.gwe.app.client.config.ClientConfig;
import org.gwe.app.client.config.XMLClientConfigReader;
import org.gwe.integration.slicer.model.GroupModel;
import org.gwe.integration.slicer.model.param.PMEnumString;
import org.gwe.integration.slicer.model.param.PMString;
import org.gwe.p2elv2.P2ELSyntaxException;
import org.gwe.persistence.model.HeadResourceInfo;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.order.p2el.POrderDescriptor;
import org.gwe.utils.IOUtils;
import org.gwe.utils.StringUtils;
import org.gwe.utils.VelocityUtils;
import org.gwe.utils.cmd.ArgsList;
import org.gwe.utils.cmd.OptionableAppTemplate;
import org.gwe.utils.rex.REXUtils;
import org.gwe.utils.security.CredentialNotFoundException;

/**
 * @author Marco Ruiz
 * @since Jan 15, 2008
 */
public class GWECLMProxyApp extends AbstractCLMProxyApp {

	private static Log log = LogFactory.getLog(GWECLMProxyApp.class);
	
	private static String VAR_SLICER_HOME = "${SLICER_HOME}";
	private static String ERROR_FILTER_NAME = "<filter-name>Error Message</filter-name>"; 

	public static void main(String[] args) throws Exception { 
		proxyAppClassMain(GWECLMProxyApp.class, new ArgsList(args)); 
	}
	
	private OptionableAppTemplate template;
	private GroupModel settings;
	private PMEnumString optClusterDesc;
	private PMString optVariables;

	private WrapperCLMProxyApp proxiedModule;

	private ClientConfig appConfig;
	
	private ProgressTracker progressTracker = new ProgressTracker() {
		public void trackProgress(String msg) {
			msg = "<filter-start>" + ERROR_FILTER_NAME + "<filter-comment>" + msg + "</filter-comment></filter-start>";
			msg += "<filter-end>"  + ERROR_FILTER_NAME + "<filter-time>0</filter-time></filter-end>";
			System.out.println(msg);
			ProgressTracker.CONSOLE_TRACKER.trackProgress(msg);
        }
	};

	public GWECLMProxyApp(String proxiedModuleName) throws IOException {
		this.proxiedModule = new WrapperCLMProxyApp(proxiedModuleName);
		try {
	        this.appConfig = new ClientConfig(new XMLClientConfigReader(null));
	        this.appConfig.setTracker(progressTracker);
        } catch (FileNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (CredentialNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		settings = new GroupModel("GWE Settings", "Slicer3 GWE Settings");

		List<String> clustersNamesList = new ArrayList<String>();
		for (HeadResourceInfo daemonInfo : appConfig.getGrid().getHeadResources()) clustersNamesList.add(daemonInfo.getName());
		String[] clustersNames = clustersNamesList.toArray(new String[]{});
		
		optClusterDesc = settings.addParam(new PMEnumString("Cluster", "gweCluster", "Name of the cluster descriptor to use to contact GWE daemon", null, clustersNames));
		optVariables   = settings.addParam(new PMString("Iteration Variables", "gweIterationVariables", "Variables to iterate to generate the commands to run in the cluster"));
		
		template = new OptionableAppTemplate(optClusterDesc, optVariables);
	}

	public String getIterVariables() { return template.getArg(optVariables).replace(";", " "); }
	
	private HeadResourceInfo getClusterSelected() {
	    return appConfig.getGrid().getHeadResource(template.getArg(optClusterDesc));
    }
	
	public String generateProxyXML() {
		String gweGroup = VelocityUtils.merge("group", settings, IOUtils.readClassPathFile("standardGroupModel.vm"));
		String proxyXML = proxiedModule.generateProxyXML().replaceFirst("<parameters>", gweGroup + "<parameters>");
		proxyXML = proxyXML.replaceAll("image>", "string>");
		proxyXML = proxyXML.replaceAll("<image ", "<string ");
		proxyXML = proxyXML.replaceAll("directory>", "string>");
		proxyXML = proxyXML.replaceAll("file>", "string>");
		proxyXML = proxyXML.replaceAll("integer>", "string>");
		proxyXML = proxyXML.replaceAll("float>", "string>");
		proxyXML = proxyXML.replaceAll("double>", "string>");
		proxyXML = proxyXML.replaceFirst("</title>", " - GWE Powered</title>");
		proxyXML = proxyXML.replaceFirst("</description>", "\n\nPowered by GWE</description>");
		return proxyXML;
	}

	public String runProxyApp(String[] args) throws P2ELSyntaxException {
		template.loadArgs(args);
		
		// Read daemon info from configuration associated with the cluster selected in Slicer UI 
		HeadResourceInfo daemonInfo = getClusterSelected();
        
        // Create P2EL command
		String moduleInvocation = proxiedModule.getModuleInvocation(VAR_SLICER_HOME);
		String moduleArgs = StringUtils.getArrayAsStr(template.getWithoutOptionsArgs(args));
		String stmt = generateP2ELStatement(getIterVariables(), moduleInvocation, moduleArgs);
		
		try {
            queueOrder(stmt, null);
        } catch (Exception e) {
        	progressTracker.trackProgress(getExceptionMessage(e));
			log.error(e);
			System.exit(1);
        }
        return "";
	}

	public String generateP2ELStatement(String p2elVars, String moduleInvocation, String moduleArgs) {
		String[] functionNames = GWEAppContext.getP2ELFunctionNames().toArray(new String[]{});
		String[] regexps = new String[]{"\\$(" + REXUtils.options(functionNames) + ")\\(([^\\(\\),]*[,]?){1,}\\)"};
		
		int varCount = 1;
		List<List<String>> matches = REXUtils.findRepeatsStrong(moduleArgs, regexps, true);
		for (List<String> parts : matches) {
	        String varRef = "${VAR_" + varCount++ + "}";
	        String functionInvocation = parts.get(0);
	        int startIndex = moduleArgs.indexOf(functionInvocation);
	        int endIndex = startIndex + functionInvocation.length();
	        moduleArgs = moduleArgs.substring(0, startIndex) + varRef + moduleArgs.substring(endIndex); 
			p2elVars += " " + varRef + "=" + functionInvocation;
        }
		
		return p2elVars + " " + moduleInvocation + " " + moduleArgs;
	}

	public void queueOrder(String stmt, String email) throws ServerAPIConnectionException, PasswordMismatchException, RemoteException, P2ELSyntaxException, InstallerException {
		Session4ClientAPIEnhancer session = appConfig.getSessionsRepository().getSession(getClusterSelected(), true);

		// Create monitoring listener
		GWECLMProxyAppEventListener listener = monitorDaemonEvents();
		
		// Queue Order
		OrderInfo order = new OrderInfo(new POrderDescriptor<String>(stmt), email);
		order.setResultParserClass(SlicerResultParser.class);
		try {
			progressTracker.trackProgress("Queueing order...");
			int orderId = session.queueOrder(order).getId();
			progressTracker.trackProgress("Order '" + orderId + "' queued!");
			// Report progress for order just queued
			// TODO: Replace with commands count read from daemon after it expanded the order
			if (listener != null) 
				listener.reportProgressFor(orderId);
		} catch (GWEDomainException e) {
			// IMPOSSIBLE! Abstract exception super class of all GWE level exceptions. The specific subclasses generated will be thrown
		}
	}
	
	private String getExceptionMessage(Exception e) {
		if (e instanceof ServerAPIConnectionException) return "Unable to connect to the GWE daemon"; 
		if (e instanceof PasswordMismatchException)    return "GWE password mismatch error"; 
		if (e instanceof P2ELSyntaxException)          return "P2EL syntax error";
		if (e instanceof RemoteException)              return "Daemon reported error '" + e.getMessage() + "'";
		return "Error: " + e.getMessage();
	}

	private GWECLMProxyAppEventListener monitorDaemonEvents() {
		try {
			GWECLMProxyAppEventListener evListener = new GWECLMProxyAppEventListener();
			appConfig.createDaemonConfig(getClusterSelected()).createAPILink().createEventMonitor().monitorEvents(evListener, new DefaultEventFilter(null));
			return evListener;
		} catch (Exception e) {
			// FIXME: Try to recover gracefully from an error that will prevent to report execution progress
		}
		return null;
	}
}
