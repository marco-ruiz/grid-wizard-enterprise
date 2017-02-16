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

package org.gwe.persistence.model;

/**
 * @author Marco Ruiz
 * @since Feb 24, 2008
 */
enum OSType   { APPLE, LINUX, SUN, UNKNOWN, WINDOWS; }
enum ARCHType { PPC, PPC_64, SPARC, UNKNOWN, X86, X86_64; }

public enum PlatformType {
	WIN32(OSType.WINDOWS, ARCHType.X86),
	WIN64(OSType.WINDOWS, ARCHType.X86_64),
	
	MAC_X86(OSType.APPLE, ARCHType.X86),
	MAC_PPC(OSType.APPLE, ARCHType.PPC), 
	MAC_PPC_64(OSType.APPLE, ARCHType.PPC_64), 
	
	LINUX_X86(OSType.LINUX, ARCHType.X86), 
	LINUX_X86_64(OSType.LINUX, ARCHType.X86_64), 
	LINUX_PPC(OSType.LINUX, ARCHType.PPC),
	LINUX_PPC_64(OSType.LINUX, ARCHType.PPC_64),
	LINUX_SPARC(OSType.LINUX, ARCHType.SPARC), 
	
	SOLARIS_INTEL(OSType.SUN, ARCHType.X86_64),  
	SOLARIS_SPARC(OSType.SUN, ARCHType.SPARC), 
	
/*
	AIX(OSType.WINDOWS, ARCHType.X86_64),
	AIX_PPC(OSType.WINDOWS, ARCHType.X86_64),
	HP_UX(OSType.WINDOWS, ARCHType.X86_64),
	IRIX(OSType.WINDOWS, ARCHType.X86_64);
*/

	UNKNOWN(null, null);

	private OSType osType;
	private ARCHType archType;
	
	PlatformType(OSType osType, ARCHType archType) {
		this.osType = osType;
		this.archType = archType;
	}
	
	public static PlatformType detectRunningPlatform() {
		OSType runningOS = detectOS();
		ARCHType runningArch = detectArch();
		
		for (PlatformType platform : PlatformType.values()) {
	        if (platform.osType != null && platform.archType != null && 
	        	platform.osType.equals(runningOS) && platform.archType.equals(runningArch)) 
	        	return platform;
        }
		return UNKNOWN;
	}
	
	public static PlatformType getByName(String name) {
		for (PlatformType platform : PlatformType.values()) {
	        if (name.equals(platform.name())) 
	        	return platform;
        }
		return UNKNOWN;
	}
	
	private static OSType detectOS() {
		String osName = System.getProperty("os.name").toLowerCase();
		assert osName != null;
		if (osName.startsWith("mac os x")) 	return OSType.APPLE;
		if (osName.startsWith("windows")) 	return OSType.WINDOWS;
		if (osName.startsWith("linux")) 	return OSType.LINUX;
		if (osName.startsWith("sun")) 		return OSType.SUN;
		return OSType.UNKNOWN;
	}

	private static ARCHType detectArch() {
		String osArch = System.getProperty("os.arch").toLowerCase();
		assert osArch != null;
		if (osArch.equals("x86") || 
			osArch.equals("i386")) 			return ARCHType.X86;
		if (osArch.startsWith("amd64") || 
			osArch.startsWith("x86_64")) 	return ARCHType.X86_64;
		if (osArch.equals("ppc")) 			return ARCHType.PPC;
		if (osArch.startsWith("ppc")) 		return ARCHType.PPC_64;
		if (osArch.startsWith("sparc")) 	return ARCHType.SPARC;
		return ARCHType.UNKNOWN;
	}
	
	public static OSType detectOSType() {
		return null;
	}
}

/*
* Apple (G3, G4): name-apple-ppc
* Apple (G5): name-apple-ppc_64
* Apple (Intel): name-apple-x86
* Linux (i686): name-linux-x86
* Linux (Intel/AMD 64): name-linux-x86_64
* Linux (sparc): name-linux-sparc
* Linux (PPC 32 bit): name-linux-ppc
* Linux (PPC 64 bit): name-linux-ppc_64
* Windows XP/Vista (i686): name-windows-x86
* Windows XP/Vista (Intel/AMD 64): name-windows-x86_64
* Sun Solaris (Blade): name-sun-sparc
* Sun Solaris (Intel 64 bit): name-sun-x86_64
*/

