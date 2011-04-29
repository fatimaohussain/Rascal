/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.utils.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;


public class HTTPStreamer implements HTTPStreamerConstants{
	
	private String boundary;
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	public HTTPStreamer(){
		
	}
	
	public HTTPStreamer(File file, long id){
	
            logger.info("Entering HTTPStreamer");

            URLConnection urlConn = getURLConnection();
		writeData(urlConn, file, id);
               // System.out.println("url" + urlConn);
		//System.out.println(readResponse(urlConn));
		String out = readResponse(urlConn); // letting is go nowhere for now
	}
	
	public HTTPStreamer(String fname, byte[] data, long id){
		throw new RuntimeException("Not yet implemented");
	}
	
	private URLConnection getURLConnection(){
		URLConnection urlConn = null;
		
		try {
			URL url = new URL(RASCAL_HTTP_SERVER_URL);
			// create a boundary string
			boundary = MultiPartFormOutputStream.createBoundary();
			urlConn = MultiPartFormOutputStream.createConnection(url);
			urlConn.setRequestProperty("Accept", "*/*");
			urlConn.setRequestProperty("Content-Type", 
				MultiPartFormOutputStream.getContentType(boundary));
			// set some other request headers...
			urlConn.setRequestProperty("Connection", "Keep-Alive");
			urlConn.setRequestProperty("Cache-Control", "no-cache");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

                logger.info("Getting URL" + urlConn);
		return urlConn;
	}
	
	private void writeData(URLConnection urlConn, File file, long id){
		try {
			MultiPartFormOutputStream out = 
				new MultiPartFormOutputStream(urlConn.getOutputStream(), boundary);
			// write a text field element
			out.writeField("MAX_FILE_SIZE", "30000000");
			out.writeField("id", id);
			// upload a file

			out.writeFile("userfile", MIME_TYPE, file);
			// can also write bytes directly
			//out.writeFile("myFile", "text/plain", "C:\\test.txt", 
			//	"This is some file text.".getBytes("ASCII"));
			
                        out.close();

                        logger.info("HTTP: Written and closed: file " + file.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String readResponse(URLConnection urlConn){
		StringBuffer response = null;
		try {
			response = new StringBuffer();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(urlConn.getInputStream()));
				String line = "";
				while((line = in.readLine()) != null) {
					 response.append(line+"\n");
				}
				in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                //logger.info("Read response" + response.toString());
		return response.toString();

	}

	public  void endStreaming(long id) {
		URLConnection urlConn = getURLConnection();
		try {
			MultiPartFormOutputStream out = 
				new MultiPartFormOutputStream(urlConn.getOutputStream(), boundary);
			// write a text field element
			out.writeField("MAX_FILE_SIZE", "30000000");
			out.writeField("id", id);
			// upload a file
			out.writeFile("userfile", MIME_TYPE, "DONE.all","All Done!".getBytes("ASCII"));
			// can also write bytes directly
			//out.writeFile("myFile", "text/plain", "C:\\test.txt", 
			//	"This is some file text.".getBytes("ASCII"));
			out.close();
			readResponse(urlConn);
                        logger.info("end streaming");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
