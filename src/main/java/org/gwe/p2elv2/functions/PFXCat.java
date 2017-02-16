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

package org.gwe.p2elv2.functions;


/**
 * @author Marco Ruiz
 * @since Aug 11, 2008
 */
public class PFXCat {} 

// No loger necessary: implemented as a macro using the more abstract and versatile PFXPath function

/*
extends PFunction {

	private static Log log = LogFactory.getLog(PFXCat.class);

	public PFXCat() { super("xcat"); }

    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
        PVarValueSpace result = new PVarValueSpace();
		try {
            InputStream xcatFileIS = new FileInputStream(params.get(0));
//            InputStream xcatFileIS = new FileLink(params.get(0), ctx.getKeys()).createHandle().getInputStream();
			CatalogDocument doc = CatalogDocument.Factory.parse(xcatFileIS);
            Entry[] entries = doc.getCatalog().getEntries().getEntryArray();
            for (Entry entry : entries) result.add(createVarValue(entry));
        	return result;
        } catch (Exception e) {
        	log.info(e);
        }
		return result;
    }

	private PVarValue createVarValue(Entry entry) {
		return new PVarValue(entry.getURI());
	}
	
	private PArrayVarValue createVarValue2(Entry entry) {
	    PArrayVarValue value = new PArrayVarValue(new HashMap<String, PVarValue>());
	    value.addDimensionalValue("",            "");
	    value.addDimensionalValue("URI",         entry.getURI());
	    value.addDimensionalValue("CACHEPATH",   entry.getCachePath());
	    value.addDimensionalValue("NAME",        entry.getName());
	    value.addDimensionalValue("ID",          entry.getID());
	    value.addDimensionalValue("DESCRIPTION", entry.getDescription());
	    value.addDimensionalValue("CONTENT",     entry.getContent());
	    value.addDimensionalValue("FORMAT",      entry.getFormat());
	    return value;
    }
*/
