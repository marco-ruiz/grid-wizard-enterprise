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

package org.gwe.utils.rex;

/**
 * @author Marco Ruiz
 * @since Jul 23, 2008
 */
public enum REXBrackets {
    PARENTHESES('(', ')'),
	BRACKETS('[', ']'),
	BRACES('{', '}');
    
    private char start;
    private char end;
    
    REXBrackets(char start, char end) {
    	this.start = start;
    	this.end = end;
    }

	public String encloseAnything() {
		return enclose("([^\\" + start + "\\" + end + "]*?)");
	}

	public String enclose(String content) {
		return "\\" + start + content + "\\" + end;
	}
}