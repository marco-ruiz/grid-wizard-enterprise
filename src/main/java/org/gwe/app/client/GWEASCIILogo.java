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

package org.gwe.app.client;

/**
 * @author Marco Ruiz
 * @since Feb 6, 2008
 */
public class GWEASCIILogo {

	public static final String GWE = 
		"         __      __       \n" +
		"    ____/  \\    /  \\___   \n" +
		"   / __ \\   \\/\\/   /__ \\  \n" +
		"  / /_/  \\        / \\_\\ \\ \n" +
		"  \\___  / \\__/\\__/\\  ___/ \n" +
		" /_____/           \\_____\\";
	
	public static final String prefixLogo(String logo, String preffix) {
		String result = logo.replaceAll("\n", "\n" + preffix);
		return preffix + result;
	}

	public static void main(String[] args) {
		System.out.println("==============================================");
		System.out.println(GWEASCIILogo.prefixLogo(GWEASCIILogo.GWE, "\t") + "\n");
		System.out.println("\tWelcome to GWE !");
		System.out.println("\tRelease Version: " + args[0]);
		System.out.println("\n==============================================\n\n");
	}
}
