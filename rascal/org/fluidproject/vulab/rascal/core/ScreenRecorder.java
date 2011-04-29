/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;

import org.fluidproject.vulab.rascal.core.utils.FastFrameBuffer;
import org.fluidproject.vulab.rascal.core.utils.FileStreamer;
import org.fluidproject.vulab.rascal.core.utils.FrameWriter;
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;


public class ScreenRecorder {
	FastFrameBuffer buffer;
	FrameWriter fwriter; //, fwriter2, fwriter3;
	FileStreamer fstreamer;
	ScreenCapture scapture;
        CountDownLatch doneSignal;
        CountDownLatch fWriterStartSignal;
        CountDownLatch fStreamerStartSignal;


	public static final String DIRNAME = System.getProperty("user.home") + File.separator + "screencast";
        public static final String AUDIO_DIRNAME = System.getProperty("user.home") + File.separator + "audio";
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	public ScreenRecorder(){
		logger.info("Entering Screen Recorder");

                //A signal to determine if the file writers have finished
                doneSignal = new CountDownLatch(1);

                //A sginal to determine if the file writer can start
                fWriterStartSignal = new CountDownLatch(1);

                //A signal to determine if the file streamer can start
                fStreamerStartSignal = new CountDownLatch(1);

                buffer = new FastFrameBuffer(new File(DIRNAME), fWriterStartSignal);
		/* For Testing Purposes
		System.out.println("Path (Screen Recorder):" + DIRNAME); */
	}
	
	public void start() throws IOException{
                logger.info("Entering start()");
                scapture = new ScreenCapture(buffer);
                //scapture.setPriority(Thread.MAX_PRIORITY);
		scapture.start();

                /*
                 * The scapture thread is started and the rest of threads are waiting. 
                 * The file writer starts once a screen shot is captured and stored in the 
                 * buffer
                 */

                try {
                   fWriterStartSignal.await();
                }catch(InterruptedException ex){

                }

                logger.info("Starting FrameWriter");

                fwriter = new FrameWriter(buffer,new File(DIRNAME),fStreamerStartSignal , doneSignal);
		//fwriter2 = new FrameWriter(buffer,new File(DIRNAME),fStreamerStartSignal,doneSignal);
		//fwriter3 = new FrameWriter(buffer,new File(DIRNAME),fStreamerStartSignal,doneSignal);

                /*
                 * The streamer thread waits until a file is written by the file writer
                 * and is available for streaming.
                 */
                 try {
                   fStreamerStartSignal.await();
                }catch(InterruptedException ex){

                }

                logger.info("Starting FileStreamer");
                fstreamer = new FileStreamer(new File(DIRNAME));
		
                logger.info("Exiting start()");
	}

        /*
         * Stops the sCapture thread.
         * No more screen shots are captured.
         */
        public void stopSCapture(){
                
            scapture.stop();
        }

        /*
         * It stops the remaining threads i.e. the file writer and file streamer threads.
         */
	public void stop(){
		logger.info("Entering stop()");

               //scapture.stop();

               	fwriter.interrupt();

                logger.info("Writer interrupted ");
		//fwriter2.interrupt();
                //logger.info("Writer2 interrupted ");
		//fwriter3.interrupt();
                //logger.info("Writer3 interrupted ");

                /*
                 * The streamer thread waits for the file writer to finish, before it 
                 * does the final streaming. Otherwise, the streamer thread could stop streaming 
                 * and the file writer thread might still be writing some files which will not be streamed.  
                 */
                try {
                    doneSignal.await();
                }catch(InterruptedException ex){

                }

                logger.info("Writers Done ");
                
                fstreamer.finalStreaming();
                logger.info("Exiting stop()");
	}

}
