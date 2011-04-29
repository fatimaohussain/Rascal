/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/

package org.fluidproject.vulab.rascal.core;


import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import org.fluidproject.vulab.rascal.core.utils.FastFrameBuffer;
import org.fluidproject.vulab.rascal.core.utils.Frame;
//import org.fluidproject.vulab.rascal.core.utils.FrameBuffer;
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;
import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;


public class ScreenCapture {
	
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	FastFrameBuffer buffer;
	
	private Robot robot = null;
         int counter = 0;
	//private Thread videoThread;
        private static final int CAPTURE_WAIT = 300;
        private CaptureVideoThread videoThread;

	public ScreenCapture(FastFrameBuffer buffer) {
	
            logger.info("Entering ScreenCapture");
            this.buffer = buffer;
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			robot = null;
			e.printStackTrace();
		}
                        logger.info("Exiting ScreenCapture");
	}

	public void start() {
                        logger.info("Entering start");
		captureVideo();
                            logger.info("Exiting start");
	}

	public void stop() {
		logger.info("STOP signal received");
                videoThread.setStopFlag();
		videoThread.interrupt();
	}

     
	public static BufferedImage compress(BufferedImage image, float quality) {
		try {
			logger.info("Entering compress()");
			Date now = new Date();
			Iterator<ImageWriter> writers = ImageIO
					.getImageWritersBySuffix("jpeg");
			ImageWriter writer = writers.next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer.setOutput(ImageIO.createImageOutputStream(out));
			writer.write(null, new IIOImage(image, null, null), param);
			byte[] data = out.toByteArray();
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			
			out.close();
			BufferedImage result = ImageIO.read(in);
			in.close();
			writer = null;
			writers = null;
			param = null;
			Date now2 = new Date();
			logger.info("compress() took:"+(now2.getTime() - now.getTime()));
			logger.info("Exiting compress()");
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage scale(BufferedImage image, float scale) {
		Date now = new Date();
		int width = (int)(image.getWidth()*scale);
		int height = (int)(image.getHeight()*scale);
		Image img = image.getScaledInstance(width,height, Image.SCALE_SMOOTH);
		image.getGraphics().dispose();
		BufferedImage result = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
		result.createGraphics().drawImage(img, 0, 0, null);
		img = null;
		image = null;
		Date now2 = new Date();
		logger.info("scale() took:"+(now2.getTime() - now.getTime()));
		return result;
	
	}



	private void captureVideo() {
	
                        logger.info("Entering Video Capture");
            try {

			//videoThread = new Thread(new CaptureVideoThread());
                        videoThread = new CaptureVideoThread();
			//videoThread.setPriority(Thread.MAX_PRIORITY);
                        //videoThread.setPriority(Thread.NORM_PRIORITY + 2);
                        videoThread.start();

		} catch (Exception e) {
			e.printStackTrace();

		}// end catch
	}// end captureAudio method

	private class CaptureVideoThread extends Thread {
                private volatile boolean stopFlag = false;

                
		public void run() {

			try {
				Rectangle screenSize = new Rectangle(Toolkit
						.getDefaultToolkit().getScreenSize());
				while (!stopFlag) {

                                    long time = System.currentTimeMillis();

                                    //Capture a screen shot
					BufferedImage image = robot
							.createScreenCapture(screenSize);
					long robotTime = System.currentTimeMillis()- time;

					PointerInfo pInfo = MouseInfo.getPointerInfo();
					Point point = pInfo.getLocation();
					image = drawCursor(image, point);

                                        //a counter to keep track of the no. of frames for logging purposes
					counter++;
                                        Date now = new Date();
					long index = now.getTime();
					Frame frame = new Frame(image,index);

                                        logger.info("Adding new frame no. " + counter + " to buffer " + frame.toString());

                                        buffer.addFrame(frame);
                                        long elapsedTime = System.currentTimeMillis()- time;

                                        logger.info("Image Capture time  " + robotTime  + " Capture + frame Adding Time " + elapsedTime);

                                        long remainingWait = CAPTURE_WAIT - elapsedTime;

                                        //Determines how much the time the thread should sleep before invoking again

                                        if (remainingWait >  0 )
                                        {
                                            Thread.sleep(remainingWait);
                                        }else {
                                            Thread.sleep(1);
                                        }
					//Thread.sleep(250);
				}
			} catch (InterruptedException e) {

                            logger.info("Video Thread Interrupted");
				// This means the data collection is done
				// saveFrames();
			} catch (HeadlessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		}

                public void setStopFlag(){
                    stopFlag = true;
                }

	}

        /*
         * This method drwas the mouse cursor in the image.
         */
	public static BufferedImage drawCursor(BufferedImage image, Point p) {
		Graphics2D g = image.createGraphics();
		g.setBackground(Color.RED);

		g.clearRect((int) p.getX(), (int) p.getY(), 10, 10);

		g.dispose();

		return image;
	}

}