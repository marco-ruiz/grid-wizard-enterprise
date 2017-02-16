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

package org.gwe.app.client.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Marco Ruiz
 * @since Feb 27, 2009
 */
public class ImageServlet extends HttpServlet {
	
    private String imageNotFoundFileName;

	public ImageServlet(String imageNotFoundFileName) {
    	this.imageNotFoundFileName = imageNotFoundFileName;
    }

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = request.getParameter("filename");
    	
        FileInputStream in = null; 
        try {
        	in = getFileInputStream(response, filename);
        } catch (Exception e) {
        	in = getFileInputStream(response, imageNotFoundFileName);
        }

        OutputStream out = response.getOutputStream();
    
        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = in.read(buf)) >= 0) 
        	out.write(buf, 0, count);

        in.close();
        out.close();
    }

	private FileInputStream getFileInputStream(HttpServletResponse response, String filename) throws IOException {
	    // Get the absolute path of the image
        ServletContext ctx = getServletContext();
    
        // Get the MIME type of the image
        String mimeType = ctx.getMimeType(filename);
        if (mimeType == null) {
            String errorMsg = "Could not get MIME type of " + filename;
			ctx.log(errorMsg);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(errorMsg);
        }
    
        // Set content type
        response.setContentType(mimeType);
    
        // Set content size
        File file = new File(filename);
        response.setContentLength((int)file.length());
    
        return new FileInputStream(file);
    }
}
