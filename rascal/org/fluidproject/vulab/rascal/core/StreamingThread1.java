/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core;

import java.awt.image.BufferedImage;
import java.io.File;

import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;

public class StreamingThread1 implements Runnable {

	private HTTPStreamer streamer;
	private File file;
	
	public StreamingThread1(File file){
		this.file = file;
	}
	
	public void run() {
		streamer = new HTTPStreamer(file,SessionFactory.getSession());
		//System.out.println("*******************************************");
                //System.out.println("file deleted" + file.toString());
                file.delete();

	}

}
