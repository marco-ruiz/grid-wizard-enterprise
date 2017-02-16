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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.gwe.p2elv2.PFunction;
import org.gwe.p2elv2.PStatementContext;
import org.gwe.p2elv2.PVarValueSpace;

/**
 * @author Marco Ruiz
 * @since Aug 4, 2008
 */
public class PFMD5Hex extends PFunction {
	
    public static final String FUNCTION_NAME = "md5Hex";

	public PFMD5Hex() { super(FUNCTION_NAME); }
    
    public PVarValueSpace calculateValues(List<String> params, PStatementContext ctx) {
    	PVarValueSpace result = new PVarValueSpace();
		for (String param : params) result.add(createHashHex(param));
		return result;
    }

	private String createHashHex(String param) {
	    byte[] hash = createMD5Hash(param);
	    return bytesToHex(hash);
    }
    
    private byte[] createMD5Hash(String key) {
    	MessageDigest longHash;
        try {
            longHash = MessageDigest.getInstance("MD5");
    		longHash.update(key.getBytes());
    		return longHash.digest();
        } catch (NoSuchAlgorithmException e) {
        	// TODO: Handle!!!
//        	log.fatal("Security not available because basic infrastructure to provide it is not available.", e);
        }
        return null;
    }
    
	public String bytesToHex(byte[] b) {
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
	
	public static void main(String[] args) {
		PFMD5Hex function = new PFMD5Hex();
		System.out.println(function.createHashHex("user-1"));
		System.out.println(function.createHashHex("user-2"));
		System.out.println(function.createHashHex("http://host/path/file-1"));
		System.out.println(function.createHashHex("http://host/path/file-2"));
	}
}

