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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gwe.utils.rex.config.REXField;

/**
 * @author Marco Ruiz
 * @since Jul 26, 2008
 */
public class REXUtils {

	public static String repeat(String expr, int min) {
		return repeat(expr, min, -1);
	}
	
	public static String repeat(String expr, int min, int max) {
		String maxStr = max < 0 ? "" : "" + max;
		return group(expr) + "{" + min + "," + maxStr + "}";
	}
	
	public static String options(String... options) {
		StringBuffer result = new StringBuffer();
		for (int idx = 0; idx < options.length - 1; idx++) result.append(options[idx] + "|");
		return group(result + options[options.length - 1]);
	}
	
	public static String group(String content) {
		return "(" + content + ")";
	}
	

	public static List<String> extractGroups(String text, String regExp, int... groupIds) {
		List<String> result = new ArrayList<String>(groupIds.length);
        Pattern pattern = Pattern.compile(regExp);
	    Matcher matcher = pattern.matcher(text);
	    if (matcher.find()) {
	    	for (int idx = 0; idx < groupIds.length; idx++) 
	    		result.add(matcher.group(groupIds[idx]));
		}
		return result;
	}

	public static List<List<String>> findRepeats(String text, String[] regExps, boolean trim) {
		String wholeRegExp = "";
		for (String regExp : regExps) wholeRegExp += regExp;
		List<String> matches = findRepeats(text, wholeRegExp, trim);
		
		List<List<String>> result = new ArrayList<List<String>>();
		for (String match : matches) result.add(findSequentialMatches(match, regExps));
		return result;
	}

	public static List<List<String>> findRepeatsStrong(String text, String[] regExps, boolean trim) {
		String wholeRegExp = "";
		for (String regExp : regExps) wholeRegExp += regExp;
		List<String> matches = findRepeats(text, wholeRegExp, trim);
		
		List<List<String>> result = new ArrayList<List<String>>();
		for (String match : matches) result.add(findSequentialMatchesStrong(match, regExps));
		return result;
	}

	public static List<String> findRepeats(String text, String regExp, boolean trim) {
		List<String> result = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regExp);
	    Matcher matcher = pattern.matcher(text);
	    while (matcher.find()) {
	        String match = matcher.group();
	        if (match != null && !match.equals("")) result.add(trim ? match.trim() : match);
        }
		return result;
	}

	public static List<String> findSequentialMatches(String source, List<REXField> patFields) {
		String[] regExps = new String[patFields.size()];
		for (int idx = 0; idx < patFields.size(); idx++) regExps[idx] = patFields.get(idx).getPattern();
		return findSequentialMatches(source, regExps);
	}

	public static List<String> findSequentialMatches(String source, String... regExps) {
		List<String> result = new ArrayList<String>();
		for (String regExp : regExps) {
			Matcher matcher = Pattern.compile(regExp).matcher(source);
			if (matcher.find()) {
				result.add(matcher.group());
				source = source.substring(matcher.end());
			} else {
				result.add(null);
			}
        }
		return result;
	}

	public static List<String> findSequentialMatchesStrong(String source, String... regExps) {
		List<String> result = new ArrayList<String>();

		for (int index = 0; index < regExps.length; index++) {
			String wholeRegExp = createRegExp(index, regExps);
			Matcher matcher = Pattern.compile(wholeRegExp).matcher(source);
			if (matcher.find()) {
				result.add(matcher.group(1));
				source = source.substring(matcher.end(1));
			}
		}

		return result;
	}
	
	private static String createRegExp(int fromIndex, String[] regExps) {
		String result = "(" + regExps[fromIndex] + ")";
		for (fromIndex++; fromIndex < regExps.length; fromIndex++)
	        result += regExps[fromIndex];
	        
		return result;
	}
}

