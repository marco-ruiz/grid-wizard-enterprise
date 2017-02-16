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
import java.util.List;

/**
 * @author Marco Ruiz
 * @since Feb 18, 2008
 */
public class StringUtils {

	public static String getArrayAsStr(String[] array) {
		String result = " ";
		for (String arg : array) result += arg + " ";
		return result.trim();
	}

	public static String getArrayAsStr(String[] array, String separator) {
		String result = "";
		for (int idx = 0; idx < array.length - 1; idx++) result += array[idx] + ",";
		return result + array[array.length - 1];
	}

	public static String[] removeArgs(String[] args, int start, int count) {
		String[] trimmedArgs = new String[args.length - count];
		for (int idx = 0; idx < start; idx++) trimmedArgs[idx] = args[idx];
		for (int idx = start + count; idx < args.length; idx++) trimmedArgs[idx - count] = args[idx];
		return trimmedArgs;
	}

	public static String[] splitSpaceSeparated(String statement) {
		return statement.split("\\p{javaWhitespace}+");
	}

	public static List<String> split(String whole, String prefixToken) {
		List<String> result = new ArrayList<String>();
		int startIdx = 0;
		int endIdx;
		while ((endIdx = whole.indexOf(prefixToken, startIdx)) != -1) {
			if (endIdx != 0) result.add(whole.substring(startIdx, endIdx));
			startIdx = endIdx + prefixToken.length();
		}
		result.add(whole.substring(startIdx));
		
		return result;
	}
	
	public static String[] splitBoundaries(String whole, String startToken, String endToken) {
		return splitBoundaries(whole, startToken, endToken, null);
	}
	
	public static String[] splitBoundaries(String whole, String startToken, String endToken, String endTokenCancelator) {
		int startIndex = whole.indexOf(startToken);
		if (startIndex == -1) return null;
		String result = whole.substring(startIndex + startToken.length());
		int endIndex = getCorrespondingEndTokenIndex(result, endToken, endTokenCancelator);
		if (endIndex == -1) return null;
		
        // [SUFIX][START_TOKEN][ENCLOSED_PIECE][END_TOKEN][PREFIX]
		return new String[]{ whole.substring(0, startIndex), startToken, result.substring(0, endIndex), endToken, result.substring(endIndex + endToken.length())};
	}

	private static int getCorrespondingEndTokenIndex(String result, String endToken, String endTokenCancelator) {
		if (endTokenCancelator == null) return result.indexOf(endToken); 
		
	    int endIndex = 0;
		int endTokensCount = 0;
		while (true) {
			endIndex = result.indexOf(endToken, endIndex);
			if (endIndex == -1) return -1;
			endTokensCount++;
			int cancelatorsCount = getTokenCounts(result.substring(0, endIndex), endTokenCancelator);
			if (cancelatorsCount == endTokensCount - 1) return endIndex;
			endIndex += endTokenCancelator.length();
		}
    }
	
	private static int getTokenCounts(String result, String endTokenCancelator) {
		int count = 0;
		int index = 0;
		while (true) {
			index = result.indexOf(endTokenCancelator, index);
			if (index == -1) return count;
			index += endTokenCancelator.length();
			count++;
		}
    }

	public static void main1(String[] args) {
		String[] splitted = splitBoundaries("123abcdefghi456", "ab", "gh");
		for (String str : splitted) System.out.println(str);
//		String[] splitted1 = splitBoundaries("myFile=$gwe:in($gwe:anon(['http', 'sftp', 'file'])://myhost/$gwe:anon([10..100||$gwe:anon([20,30])])) $gwe:out(http://myhost/$gwe:anon([10..100||20])-)", "$gwe:", ")", "(");
		String[] splitted1 = splitBoundaries("myFile=$gwe:out(http://myhost/$gwe:anon([10..100||20]))$gwe:what()", "$gwe:out(", ")", "(");
/*
		String[] splitted2 = splitBoundaries(splitted1[2], "$gwe:", ")", "(");
		for (String str : splitted1) System.out.println(str);
		System.out.println();
		for (String str : splitted2) System.out.println(str);
*/
		
		String[] splitted3 = splitBoundaries("--file $P2EL:stage(in(a, b, c),http://myfile) --file2", "$P2EL:stage(", ")", "(");
		System.out.println();
		for (String str : splitted3) System.out.println(str);

		String[] splitted4 = splitBoundaries(splitted3[2], "(", ")");
		System.out.println();
		for (String str : splitted4) System.out.println(str);

		System.out.println(split("---aaaaa---bbbbb---cccasdasdfadsf---dd3245234kjherihwerioutyd---e", "---"));
		System.out.println(StringUtils.getArrayAsStr("---aaaaa---bbbbb---cccasdasdfadsf---dd3245234kjherihwerioutyd---e".split("---")));
	}
}
