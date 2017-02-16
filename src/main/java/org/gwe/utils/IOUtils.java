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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marco Ruiz
 * @since Jun 13, 2007
 */
public class IOUtils {
//	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String FILE_SEPARATOR = "/";

	public static boolean makeFilePath(String file) {
        return new File(IOUtils.getFilePath(file)).mkdirs();
    }
	
    public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}
	
	public static String getFileBase(String uriStr) {
		String base = uriStr.replaceAll("[*?\\[].*", "");
        if (!base.endsWith(FILE_SEPARATOR)) base = base.replaceFirst("/[^/]*$","/");
		return base;
	}
	
	public static String getFileName(String file) {
        int indexOfLastSeparator = file.lastIndexOf(FILE_SEPARATOR);
		return (indexOfLastSeparator == -1) ? "" : file.substring(indexOfLastSeparator + 1); 
	}

	public static String getFilePath(String file) {
        int indexOfLastSeparator = file.lastIndexOf(FILE_SEPARATOR);
        return (indexOfLastSeparator == -1) ? file : file.substring(0, indexOfLastSeparator);
    }
	
	public static void createLocalFolder(String fullPath) {
		new File(fullPath).mkdir();
	}
	
	public static String createLocalExecutableFile(String fullFileName, String contents) throws IOException {
//		if (mkdirs) createLocalFolder(getFilePath(fullFileName));
		FileOutputStream fos = new FileOutputStream(fullFileName, false);
		fos.close();
		Runtime.getRuntime().exec("chmod a+x " + fullFileName);
		fos = new FileOutputStream(fullFileName, false);
		fos.write(contents.getBytes());
		fos.close();
		return fullFileName;
	}
	
	public static String concatenatePaths(Object... paths) {
		if (paths == null || paths.length < 1) return "";
		Object result = paths[0];
		for (int idx = 1; idx < paths.length; idx++) 
			result = concatenatePaths(result.toString(), paths[idx].toString());
		return result.toString();
    }

	public static String concatenatePaths(String filePath, String fileName) {
		if (filePath == null || fileName == null) return null;
		if (filePath.endsWith(FILE_SEPARATOR))   filePath = filePath.substring(0, filePath.length() - 1);
		if (fileName.startsWith(FILE_SEPARATOR)) fileName = (fileName.length() == 1) ? "" : fileName.substring(1);
		return concatenate(filePath, FILE_SEPARATOR, fileName);
    }
	
	public static String concatenate(Object... args) {
		StringBuffer buf = new StringBuffer();
		for (int idx = 0; idx < args.length; idx++) buf.append(args[idx]);
		return buf.toString();
	}

	public static String readFile(File f) throws FileNotFoundException, IOException {
		return readReader(new FileReader(f));
	}

	public static String readReader(InputStreamReader fr) throws IOException {
		StringBuffer sb = new StringBuffer();
		char[] buff = new char[1024];
		for (int num_read = fr.read(buff); num_read >= 0; num_read = fr.read(buff)) {
		    for (int ii = 0; ii < num_read; ++ii) {
		        sb.append(buff[ii]);
		    }
		}
		fr.close();
		return sb.toString();
	}
	
	public static byte[] readFile(String filename) throws IOException, FileNotFoundException {
	    return (filename == null || "".equals(filename)) ? null : readStream(new FileInputStream(new File(filename)), null).toByteArray();
    }
	
	public static String readClassPathFile(String fileName) {
		InputStream is = IOUtils.class.getClassLoader().getResourceAsStream(fileName);
		StringBuffer sb = new StringBuffer();
		int nextChar;
		try {
			while ((nextChar = is.read()) != -1) sb.append((char)nextChar);
		} catch (IOException e) {
			// TODO: Do something and/or rethrow the exception
		}
		try {
			is.close();
		} catch (IOException e) {
			// Do nothing.
		}
		return sb.toString();
	}
	
	public static int pipeStreams(InputStream is, OutputStream os) throws IOException {
		return pipeStreams(is, os, false);
	}

	public static ByteArrayOutputStream readStream(InputStream is, OutputStream myOs) throws IOException {
		return readStream(is, myOs, null);
	}

	public static String readStream(InputStream is) throws IOException {
		return new String(IOUtils.readStream(is, null).toByteArray());
	}
	
	public static ByteArrayOutputStream readStream(InputStream is, OutputStream myOs, String endTag) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Set<OutputStream> osSet = new HashSet<OutputStream>();
		osSet.add(os);
		if (myOs != null) osSet.add(myOs);
		pipeStreams(is, osSet, false, endTag);
		return os;
	}

	public static int pipeStreams(InputStream is, OutputStream os, boolean closeStreams) throws IOException {
		Set<OutputStream> osSet = new HashSet<OutputStream>();
		osSet.add(os);
		return pipeStreams(is, osSet, closeStreams, null);
	}
	
	public static int pipeStreams(InputStream is, Set<OutputStream> osSet, boolean closeStreams, String endTag) throws IOException {
		String latestEndTag = "";
		long start = System.currentTimeMillis();
		int totalPiped = 0;
		InputStream in = null;
		Set<BufferedOutputStream> outs = new HashSet<BufferedOutputStream>();
		try {
			in = new BufferedInputStream(is);
			for (OutputStream os : osSet) outs.add(new BufferedOutputStream(os));

		    byte[] buffer = new byte[1024 * 64];
			while (true) {
				int amountRead = in.read(buffer);
				if (amountRead == -1) break;
				for (BufferedOutputStream out : outs) {
					out.write(buffer, 0, amountRead);
					out.flush();
				}
				if (endTag != null) {
					int shift = Math.max(latestEndTag.length() - endTag.length() - 1, 0);
					latestEndTag = latestEndTag.substring(shift) + new String(buffer, 0, amountRead);
					if (latestEndTag.contains(endTag)) break;
				}
				totalPiped += amountRead;
			}
		} finally {
			if (in != null) in.close();
			if (closeStreams)
				for (BufferedOutputStream out : outs) out.close();
		}
//		LogFactory.getLog(IOUtils.class).trace("Transfered " + totalPiped + " bytes.");
		long duration = System.currentTimeMillis() - start;
		return totalPiped;
	}
	
/*	
	public static int pipeStreams(InputStream is, Set<OutputStream> osSet, boolean closeStreams) throws IOException {
		long start = System.currentTimeMillis();
		int totalPiped = 0;
		int numRead = 0;
		try {
			while ((numRead = is.read(buffer)) >= 0) {
				for (OutputStream os : osSet) {
				    os.write(buffer, 0, numRead);
				    // TODO: Make sure flushing works for VFS. Currently it is broken and the whole flushing occurs at 
				    // output stream closing time; which represents a memory problem (the latest vfs version has problems 
				    // with zip and tar files!)
		            os.flush(); 
				}
	
	            totalPiped += numRead;
			}
		} finally {
			if (closeStreams) {
				for (OutputStream os : osSet) os.close();
			    is.close();
			}
		}
		log.trace("Transfered " + totalPiped + " bytes.");
		long duration = System.currentTimeMillis() - start;
	    return totalPiped;
	}
*/
	
	public static void serializeObject(Object obj, String fileName) throws IOException {
		serializeObject(new FileOutputStream(fileName));
	}

	public static void serializeObject(Object obj, OutputStream os) throws IOException {
	    os.write(serializeObject(obj));
	    os.close();
	}

	public static <T> byte[] serializeObject(T obj) throws IOException {
		ByteArrayOutputStream resultOS = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(resultOS);
		out.writeObject(obj);
		out.close();
		byte[] result = resultOS.toByteArray();
		resultOS.close();
		return result;
	}

	public static <T> T deserializeObject(byte[] stream) throws IOException, ClassNotFoundException {
		return (T)deserializeObject(new ByteArrayInputStream(stream));
	}

	public static <T> T deserializeObject(InputStream is) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(is);
    	T result = (T)in.readObject();
    	in.close();
    	return result;
	}
}
