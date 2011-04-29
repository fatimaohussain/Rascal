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
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;

public class FrameWriter implements Runnable {

	private FastFrameBuffer buffer;
	private Thread thread;
	private File dir;
        private final CountDownLatch doneSignal;
        private final CountDownLatch startSignal;
      
        private volatile boolean stopSignal = false;
        private volatile boolean interruptSafe = true;
        private volatile boolean firstFile = true;


        private final Object lock;
       

        public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	public FrameWriter(FastFrameBuffer buffer, File dirToWriteTo, CountDownLatch startSignal, CountDownLatch doneSignal) {
		thread = new Thread(this);
		this.buffer = buffer;
		dir = dirToWriteTo;
                /*
                 * The start and done signal determine when the thread should 
                 * start and end.
                 */
                this.startSignal = startSignal;
                this.doneSignal = doneSignal;
               
                lock = new Object();
                thread.start();

	}
	
	public void interrupt(){

            logger.info("Stoping Writer thread stop signal = true ");
            stopSignal = true;

            logger.info("Writer thread stopped");
            logger.info("Outside Iterrupt flag is  " + interruptSafe);
            //interruptSafe flag checks if it is okay to interrupt the writer thread.
            //It ensures that the writer thread is not interrupted while it is writing the files
            while(interruptSafe == false){
                synchronized(lock){
                   try{

                       logger.info("Iterrupt flag is  " + interruptSafe);
                       
                       lock.wait();
                    }catch(InterruptedException e)
                    { 
                        logger.info("Writer threadlock exception ");
                    }

                }

            }
                    
            thread.interrupt();
           
            cleanUp();
	}
	
	public void cleanUp(){

            logger.info("Clean Up ");
		while (!buffer.isEmpty()){
			writeFile();
		}
                doneSignal.countDown();
               
	}

        
	public void run() {
		//while (true) {
            logger.info("In RUn : stop signal  " + stopSignal);
            while (!stopSignal) {
			
			try {
                            logger.info("In RUn : before write file ");
                          
                                interruptSafe = false;
				if(!buffer.isEmpty()){
                                   writeFile();

                                   if(firstFile){
                                        startSignal.countDown();
                                        logger.info("Start Signal in Frame Writer ");
                                        firstFile = false;
                                    }

                                }
                                else {
                                    logger.info("buffer is empty ");
                                }
                                interruptSafe = true;
                                synchronized(lock){
                                     lock.notify();
                                }
                                
                            //}
				Thread.sleep(5);
                                //Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block

                            logger.info("Writer thread being interrupted ");
				
			}
		}

          
	}

	private void writeFile() {
		logger.info("Starting writeFile ");
                Frame f = buffer.get();

                if ( f != null) {
                    f.adjustImage();
                    f.toFile(dir);
                    logger.info("writeFile " + dir + " with image " + f.toString());
                    logger.info("Finishing writeFile ");
            } else {
                 logger.info("Frame is null ");
            }
        }


}
