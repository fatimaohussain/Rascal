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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;

import org.fluidproject.vulab.rascal.ui.ScreencastApplet;

public class FastFrameBuffer {
	private static final int BUFFER_SIZE = 15;
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());
	private BlockingQueue<Frame> queue;
	private File dir;
        private final CountDownLatch fWriterStartSfignal;
        private volatile boolean firstInsert = false ;
	
	public FastFrameBuffer(File dir, CountDownLatch fWriterStartSfignal){
		queue = new ArrayBlockingQueue<Frame>(BUFFER_SIZE,true);
		deleteDir(dir.getPath());
		dir.mkdir();
                this.dir = dir;
		this.fWriterStartSfignal = fWriterStartSfignal;

	}
	
	/*public void finalize(){

            
		//deleteDir(dir.getPath());
	}
	*/
        
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public void addFrame(Frame f) throws InterruptedException{
		
			logger.info("inserting "+f.toString());
			logger.info("FREE MEMORY:" + Runtime.getRuntime().freeMemory());
			//logger.info("queue size "+queue.size());
			queue.put(f);
                        //queue.add(f);
                        logger.info("queue size after insertion "+ queue.size());
                        //String top = (queue.isEmpty())? "EMPTY" : queue.peek().toString();
			//logger.info("Added in queue "+top);
			//logger.info("insertion complete");

                        //A signal to the writer thread to start as an insertion in the
                        //buffer is made.
                        if(!firstInsert){
                            fWriterStartSfignal.countDown();
                            firstInsert = true;
                            logger.info("Start Signal in Frame Buffer ");
                        }
		
	}
	
	public Frame get(){
		try {
                        logger.info("Entering in get");
                        String top = (queue.isEmpty())? "EMPTY" : queue.peek().toString();
			logger.info("dequeuing "+top);
			logger.info("Queue size is "+ queue.size());
			//if(! queue.isEmpty()) {
                            return queue.take();
                        //} else {
                          //  return null;
                        //}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static boolean deleteDir(String strFile) {
		// Declare variables variables
		File fDir = new File(strFile);
		String[] strChildren = null;
		boolean bRet = false;

		// Validate directory
		if (fDir.isDirectory()) {
			// -- Get children
			strChildren = fDir.list();

			// -- Go through each
			for (int i = 0; i < strChildren.length; i++) {
				bRet = deleteDir(new File(fDir, strChildren[i])
						.getAbsolutePath());
				if (!bRet) {
					return false;
				}
			}
		}
	

		// The directory is now empty so delete it
		return fDir.delete();

	}
	
}
