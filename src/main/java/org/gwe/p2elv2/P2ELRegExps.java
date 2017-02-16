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

package org.gwe.p2elv2;

import static org.gwe.utils.rex.REXUtils.group;
import static org.gwe.utils.rex.REXUtils.options;
import static org.gwe.utils.rex.REXUtils.repeat;

import org.gwe.utils.rex.REXBrackets;


/**
 * @author Marco Ruiz
 * @since Feb 12, 2008
 */
public interface P2ELRegExps {
	public static final String ID = "[a-zA-Z]\\w*";
	public static final String SPACES = "\\s*";
	public static final String EVERYTHING = ".*";
	public static final String ASSIGN = SPACES + "=" + SPACES;
	
	public static final String VAR_DIM = REXBrackets.PARENTHESES.enclose("[\\w,]*");
	public static final String VAR_ID = ID + repeat(VAR_DIM, 0, 1);
	public static final String VAR_DEC = "\\$" + REXBrackets.BRACES.enclose(group(VAR_ID));

	public static final String BRACKETED_VALUE = REXBrackets.BRACKETS.encloseAnything();
	public static final String FUNCTION = "f:" + ID + REXBrackets.PARENTHESES.encloseAnything();
	public static final String VAR_VALUE = options(BRACKETED_VALUE, FUNCTION);

	public static final String VAR_DEF = VAR_DEC + ASSIGN + VAR_VALUE;
	public static final String STMT = group("^" + repeat(SPACES + VAR_DEF, 0)) + group(EVERYTHING);
}

