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

package org.gwe.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Sep 2, 2008
 */
public class Path {
	
	private static final String BRANCH_SEPARATOR = "/";

	private List<String> branches = new ArrayList<String>();
	
	public Path(List<String> branches) {
		this(branches.toArray(new String[]{}));
	}
	
	public Path(String... branches) {
		appendBranches(branches);
	}
	
	public Path createChild(String... relativePath) {
		Path result = new Path(this.branches);
		result.appendBranches(relativePath);
		return result;
	}

	private void appendBranches(String... branches) {
		for (String branch : branches)
			this.branches.addAll(Arrays.asList(branch.split(BRANCH_SEPARATOR)));
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (String branch : branches)
	        result.append(BRANCH_SEPARATOR).append(branch);
		return result.toString();
	}
}
