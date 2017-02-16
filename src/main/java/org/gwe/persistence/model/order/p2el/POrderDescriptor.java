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

package org.gwe.persistence.model.order.p2el;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gwe.drivers.fileSystems.staging.FilesStager;
import org.gwe.p2elv2.P2ELDependentVariableNotResolvedException;
import org.gwe.p2elv2.P2ELFunctionNotSupported;
import org.gwe.p2elv2.P2ELMultiValueVarDependentOnRuntimeVarException;
import org.gwe.p2elv2.PPermutation;
import org.gwe.p2elv2.PStatementCompiler;
import org.gwe.p2elv2.model.PStatement;
import org.gwe.p2elv2.model.PVariable;
import org.gwe.persistence.model.DaemonConfigDesc;
import org.gwe.persistence.model.OrderInfo;
import org.gwe.persistence.model.VarInfo;
import org.gwe.persistence.model.live.OrderLive;
import org.gwe.persistence.model.order.JobDescriptor;
import org.gwe.persistence.model.order.OrderDescriptor;
import org.gwe.utils.rex.REXException;
import org.gwe.utils.rex.REXParser;

/**
 * 
 * @author Marco Ruiz
 * @since Sep 20, 2007
 */
public class POrderDescriptor<DAEMON_REQUEST_PARAM_TYPE extends Serializable> extends OrderDescriptor<DAEMON_REQUEST_PARAM_TYPE> {
	
	protected String stmt;
    private transient FilesStager fileStager;
	
	public POrderDescriptor() {}

	public POrderDescriptor(String stmt) {
		this.stmt = stmt;
	}

    public List<JobDescriptor> generateJobDescriptors(DaemonConfigDesc config) throws Exception {
    	PStatement stmtObj = createLocalizedStatement(config);
    	PStatementCompiler compiler = createCompiler(config, stmtObj);

        String template = stmtObj.getTemplate();
		List<JobDescriptor> result = new ArrayList<JobDescriptor>();
		for (PPermutation perm : compiler.compile()) 
	        result.add(new PJobDescriptor(perm, template));
		
	    return result;
    }
    
    public List<String> generateCommands(DaemonConfigDesc config) throws Exception {
    	PStatement stmtObj = createLocalizedStatement(config);
    	PStatementCompiler compiler = createCompiler(config, stmtObj);
    	return compiler.createStatements(compiler.compile());
    }

	private PStatement createLocalizedStatement(DaemonConfigDesc config) throws REXException {
	    String stmtPreffix = "";
    	for (VarInfo var : config.getHeadResource().getVars()) 
    		stmtPreffix = "${" + var.getName() + "}=" + var.getValue() + " ";
    	
    	PStatement stmtObj = REXParser.createModel(PStatement.class, stmtPreffix + stmt);
	    return stmtObj;
    }

	private PStatementCompiler createCompiler(DaemonConfigDesc config, PStatement stmtObj) throws P2ELFunctionNotSupported, P2ELDependentVariableNotResolvedException, P2ELMultiValueVarDependentOnRuntimeVarException {
//		PStatementCompiler compiler = new PStatementCompiler(stmtObj, null, config.getKeys(), new FilesStager(order.getWorkspaceInDaemon(config), config.getKeys()));
		return new PStatementCompiler(stmtObj, null, config.getKeys(), null);
    }

	public void initExecution(OrderLive orderLive) {
		DaemonConfigDesc config = orderLive.getConfig();
		OrderInfo order = orderLive.getInfo();
		fileStager = new FilesStager(order.getWorkspaceInDaemon(config), config.getKeys());
	}

	public void finalizeExecution(OrderLive orderLive) {
		if (orderLive.canCleanUp())
			fileStager.dispose();
	}

	public FilesStager getFileStager() {
		return fileStager;
	}

    public String toString() {
		return getP2ELStatementObj().toStringFormatted("", "\n");
	}

	public String getP2ELStatement() {
		PStatement stmtObj = getP2ELStatementObj();
		return (stmtObj != null) ? stmtObj.toStringFormatted() : stmt;
	}

	public PStatement getP2ELStatementObj() {
    	try {
	        return REXParser.createModel(PStatement.class, stmt);
        } catch (REXException e) {
        	return null;
        }
	}
	
	public List<String> getVarNames() {
		List<String> result = new ArrayList<String>();
		for (PVariable var : getP2ELStatementObj().getVars()) result.add(var.getFullName());
		return result;
	}
}

