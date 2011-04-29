/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core.utils;

import java.io.File;
import java.util.logging.Logger;
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;
import org.fluidproject.vulab.rascal.core.AudioRecorder;
import org.fluidproject.vulab.rascal.core.SessionFactory;
import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;

public class FileStreamer implements Runnable {

	private Thread thread;
	private File dir;
        private volatile boolean stopSignal = false;
        private volatile boolean interruptSafe = true;
        private final Object lock;

public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());
	
	public FileStreamer(File dirToStreamFrom){
		thread = new Thread(this);
		dir = dirToStreamFrom;
                lock = new Object();
		thread.start();
	}
	
	public void finalStreaming(){
		//stream anything left over and clean up
	

            logger.info("Entering Finalize");
            stopSignal = true;

            while(interruptSafe == false){
                synchronized(lock){
                   try{

                       //logger.info("Streamer Iterrupt flag is  " + interruptSafe);

                       lock.wait();
                    }catch(InterruptedException e)
                    {
                        logger.info("Streamer  thread lock exception ");
                    }

                }

            }

            thread.interrupt();

           
	    streamFiles();
	}
	
	public void run() {
		try {
			while (!stopSignal) {
				streamFiles();
				synchronized (this) {
					Thread.sleep(10);
                                        
				}

			}
		} catch (InterruptedException ie) {
			// stop running
                    logger.info("File streamer thread interrupted");
		}

	}
	
	private synchronized void streamFiles() {
		 logger.info("Entering Stream files");

		File[] files = dir.listFiles();
               
              
		if (files != null) {
                    interruptSafe = false;
                        logger.info("Total files: " + files.length);
			for (int i = 0; i < files.length; i++) {
                            
                            if(files[i].exists() )
                            {
                            logger.info("Total files: " + files.length+ " Streaming file no. " + i+ "  " + files[i].toString());

				new HTTPStreamer(files[i], SessionFactory.getSession());

                                 logger.info("File being deleted: "+ files[i].toString());
                                boolean deleted = files[i].delete();
                                logger.info("Delete value: "+ deleted );


                            }
                      }

		} else {
                    //This block is only for error checking and logging purposes.
                    if (dir.isDirectory()&& dir.exists())
                    {
                        logger.info("Files NULL but directory exists");
                    }
                    logger.info("Files NULL");
                }

                //To make sure it is safe to interrupt the thread.
                //We do not want to interrupt the thread if soem files are still being streamed. 
                interruptSafe = true;
                synchronized(lock){
                    lock.notify();
                }
		
            
	}
	
	

}
