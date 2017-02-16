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

import java.io.IOException;
import java.io.Serializable;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Marco Ruiz
 * @since May 21, 2008
 */
public class CompressedObject<VALUE_TYPE extends Serializable> implements Serializable {

	private static Log log = LogFactory.getLog(CompressedObject.class);

	private int originalLength;
	private byte[] compressedObject = null;
	private VALUE_TYPE targetObject = null;
	
	public CompressedObject() { nullTarget(); }

	public CompressedObject(VALUE_TYPE target) throws CompressionException { setTarget(target); }
	
	public void setTargetBlindly(VALUE_TYPE target) {
	    try {
			setTarget(target);
	    } catch (CompressionException e) {
	    	String msg = "Unusual IOException error caught trying to create a serialized stream of an object";
	    	log.warn(msg, e);
	    	throw new RuntimeException(msg, e.getCause());
	    }
    }
	
	public void setTarget(VALUE_TYPE target) throws CompressionException {
		if (target == null) {
			nullTarget();
            return;
		}
		
	    byte[] source = serializeTarget(target);
        originalLength = source.length;
        byte[] compressedTarget = new byte[originalLength];
        Deflater compresser = new Deflater(5, true);
        compresser.setInput(source);
        
        compresser.finish();
        int length = compresser.deflate(compressedTarget);
        if (length == originalLength) {
        	compressedTarget = null;
        	targetObject = target; 
        } else {
        	compressedObject = new byte[length];
        	System.arraycopy(compressedTarget, 0, compressedObject, 0, length);
        }
    }

	private byte[] serializeTarget(Serializable target) throws CompressionException {
	    try {
	        return IOUtils.serializeObject(target);
        } catch (IOException e) {
        	throw new CompressionException(e);
        }
    }

	private void nullTarget() {
	    compressedObject = null;
	    targetObject = null;
    }
	
	public VALUE_TYPE getTargetBlindly() {
		try {
	        return getTarget();
        } catch (CompressionException e) {
	        // TODO Handle better
	    	log.warn("Problems decompressing target object", e);
        	return null;
        }
	}

	public VALUE_TYPE getTarget() throws CompressionException {
		if (targetObject != null) return targetObject;
		if (compressedObject == null) return null;
		
		byte[] result = new byte[originalLength];
        
        // Decompress the bytes
        Inflater decompresser = new Inflater(true);
        decompresser.setInput(compressedObject, 0, compressedObject.length);
        try {
	        decompresser.inflate(result);
	        decompresser.end();
	        return (VALUE_TYPE) IOUtils.deserializeObject(result);
        } catch (Exception e) {
        	throw new CompressionException(e);
        }
	}
}

