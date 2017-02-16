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

package org.gwe.utils.xstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author Marco Ruiz
 * @since Jan 15, 2009
 */
public class XMLConfigFile<MODEL_TYPE> {

	private MODEL_TYPE model;
	
	public XMLConfigFile(String fileName, AliasTransf... transformations) throws FileNotFoundException {
        XStream result = new XStream(new DomDriver());
        for (AliasTransf transf : transformations) transf.transform(result);
//        result.registerConverter(new RealmAccessConverter());
        
//		InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
		InputStream is = null;
		if (is == null) is = new FileInputStream(new File(fileName));
        model = (MODEL_TYPE) result.fromXML(is);
        try {
	        is.close();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	public MODEL_TYPE getModel() {
    	return model;
    }
}

