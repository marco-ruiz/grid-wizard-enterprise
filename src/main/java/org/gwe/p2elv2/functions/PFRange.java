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

import java.util.List;

import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValueSpace;

/**
 * @author Marco Ruiz
 * @since Aug 14, 2008
 */
public class PFRange extends PFunction {

	public PFRange() { super("range"); }

    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
    	PVarValueSpace result = new PVarValueSpace();
    	if (params.size() < 2) return result;
    	
    	float start;
    	float end;
    	try {
        	start = Float.parseFloat(params.get(0));
        	end = Float.parseFloat(params.get(1));
    	} catch(NumberFormatException e) {
    		return result;
//    		throw new P2ELSyntaxException("Range start and end values has to be numeric. Found: " + start + " and " + end);
    	}

    	Float step = new Float(1);
    	String format = "x";
    	try {
    		if (params.size() == 3) {
	    		format = params.get(2);
	    		step = Float.valueOf(format);
    		}
    	} catch(NumberFormatException e) {
    		// Ignore and use the default step of 1
    	}

		NumberFormatter formatter = new NumberFormatter(format);

		result.add(formatter.getAsString(start));
		for (float curr = start + step; curr <= end; curr += step) result.add(formatter.getAsString(curr));
		return result;
    }

	class NumberFormatter {
		private int minIntSize;
		private int decimalPlaces = 0;
		private int decimalPlacesConversionFactor = 0;

		NumberFormatter(String format) {
			int pointIdx = format.indexOf('.');
			minIntSize = format.length();
			if (pointIdx != -1) {
				minIntSize = pointIdx;
				decimalPlaces = format.length() - pointIdx - 1;
				decimalPlacesConversionFactor = (int) Math.pow(10, decimalPlaces); 
			}
		}

		public String getAsString(Float number) {
			long intPart = (long) Math.abs(number);
			StringBuffer result = formattedInt(intPart, minIntSize);
			if (decimalPlaces > 0) {
				long decimals = (long)Math.round(Math.abs(number * decimalPlacesConversionFactor)) - intPart * decimalPlacesConversionFactor;
				result.append(".").append(formattedInt(decimals, decimalPlaces));
			}

			if (number < 0) result.insert(0, "-");
			return result.toString();
		}
		
		private StringBuffer formattedInt(long value, int minSize) {
			StringBuffer result = new StringBuffer(String.valueOf(value));
			for (int idx = minSize - result.length(); idx > 0; idx--) result.insert(0, "0");
			return result;
		}
	}
}
