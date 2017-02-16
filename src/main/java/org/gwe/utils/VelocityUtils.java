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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author Marco Ruiz
 * @since Dec 17, 2007
 */
public class VelocityUtils {

	static {
		try { 
			Velocity.init(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}
	
	public static String evaluate(String template) {
		return merge("model", null, template);
    }
	
	public static String merge(String modelName, Object model, String template) {
		Map<String, Object> ctxParams = new HashMap<String, Object>();
		ctxParams.put(modelName, model);
        return merge(ctxParams, template);
    }

	public static String merge(Map<String, ?> ctxParams, String template) {
	    VelocityContext context = new VelocityContext(ctxParams);
        StringWriter writer = new StringWriter();
        try {
			Velocity.evaluate(context, writer, "", template);
		} catch (Exception e) {
//			e.printStackTrace();
			return template;
		}
        
		String result = writer.getBuffer().toString();
		return result;
    }

	public static String mergeThrowingExceptions(Map<String, ?> ctxParams, String template) throws Exception {
	    VelocityContext context = new VelocityContext(ctxParams);
        StringWriter writer = new StringWriter();
        try {
			Velocity.evaluate(context, writer, "", template);
		} catch (Exception e) {
			throw e;
		}
        
		String result = writer.getBuffer().toString();
		return result;
    }
}
