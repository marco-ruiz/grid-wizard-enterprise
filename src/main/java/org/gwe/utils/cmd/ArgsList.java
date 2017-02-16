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
import java.util.Arrays;

/**
 * @author Marco Ruiz
 * @since Mar 13, 2008
 */
public class ArgsList extends ArrayList<String> {
	
	public ArgsList(String[] args) {
		addAll(Arrays.asList(args));
	}
	
	public String[] getArgs() {
		return toArray(new String[]{});
	}
	
	public String extractArgIfPrefixed(int index, String prefix) {
		return (size() > index && get(index).startsWith(prefix)) ? remove(index) : "";
	}
}
