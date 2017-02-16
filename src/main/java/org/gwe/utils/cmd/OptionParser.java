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

package org.gwe.utils.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Mar 13, 2008
 */
public class OptionParser {

	private List<String> parts;
	
	public OptionParser(String prefix, String arg, char... separators) {
		parts = new ArrayList<String>(separators.length + 1);

		if (arg == null || !arg.startsWith(prefix)) return;

		String partsArg = arg.substring(prefix.length());
		for (int idx = separators.length - 1; idx >= 0; idx--) {
	        String value = null;
			int sepIndex = partsArg.lastIndexOf(separators[idx]);
	        if (sepIndex != -1) {
	        	value = partsArg.substring(sepIndex + 1);
	        	partsArg = partsArg.substring(0, sepIndex); 
	        }
	        parts.add(0, value);
        }
        parts.add(0, partsArg);
	}
	
	public String getEle(int index, String defaultValue) {
		if (index > parts.size() - 1) return defaultValue;
		String result = parts.get(index);
		return (result != null && !"".equals(result)) ? result : defaultValue;
	}
	
	public static void main(String[] args) {
		OptionParser parser = new OptionParser("prefix=", "prefix=alfa:gama:delta@whatever-now:end", new char[]{':', ':', '@', '-', ':'});
		System.out.println(parser.getEle(0, null));
		System.out.println(parser.getEle(1, null));
		System.out.println(parser.getEle(2, null));
		System.out.println(parser.getEle(3, null));
		System.out.println(parser.getEle(4, null));
		System.out.println(parser.getEle(5, null));
		System.out.println(parser.getEle(6, null));
		System.out.println(parser.getEle(7, null));
	}
}

