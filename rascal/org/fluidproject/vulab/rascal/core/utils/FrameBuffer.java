/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.fluidproject.vulab.rascal.core.ScreenCapture;
import org.fluidproject.vulab.rascal.core.SessionFactory;
import org.fluidproject.vulab.rascal.ui.ScreencastApplet;
import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;

public class FrameBuffer {

	public static Logger logger = Logger.getLogger(ScreencastApplet.class
			.getName());

	private Hashtable<String, BufferedImage> buffer;
	File dir;
	Thread monitor, streamMonitor;

	public FrameBuffer() {
		logger.info("Entering FrameBuffer()");
		buffer = new Hashtable<String, BufferedImage>(10);
		dir = new File(".screencast");
		deleteDir(dir.getAbsolutePath());
		dir.mkdir();
		monitor = new Thread(new BufferMonitor());
		monitor.start();
		streamMonitor = new Thread(new StreamMonitor());
		streamMonitor.start();
		logger.info("Exiting FrameBuffer()");

	}

	public synchronized int getSize() {
		return buffer.size();
	}

	public void finalize() {
		logger.info("Entering finalize()");
		flushtoFile();
		monitor.interrupt();
		streamMonitor.interrupt();
		streamFiles(); // just in case
		deleteDir(dir.getAbsolutePath());
		logger.info("Exiting finalize()");

	}

	public void addFrame(String filename, BufferedImage img) {
		logger.info("Entering addFrame()");
		logger.info("Adding frame at time:" + filename);
		//img = ScreenCapture.compress(ScreenCapture.scale(img, 0.5f),0.5f);
		buffer.put(filename, img);
		//flushtoFile();
		logger.info("Exiting addFrame()");
	}

	public void flushtoFile() {
		logger.info("Entering flushtoFile()");
		logger.info("Frame Size:" + buffer.size());
		Enumeration<String> keys = buffer.keys();
		 synchronized (buffer) {

		while (keys.hasMoreElements()) {
			String str = keys.nextElement();
			logger.info("Flushing key:" + str);
			BufferedImage img = buffer.get(str);
			File file = new File(dir.getName() + "/" + str);
			try {
				ImageIO.write(img, "jpg", file);
				img = null;
				buffer.remove(str);
				logger.info("Flushing key:" + str+" DONE.");
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		}
		buffer.clear();
		// buffer = new Hashtable<String, BufferedImage>(20);
		 }
		// System.out.println("Buffer Flushed");
		// System.gc();
		logger.info("Frame Size:" + buffer.size());
		// System.out.println(Runtime.getRuntime().freeMemory());

		logger.info("Exiting flushtoFile()");
	}

	private synchronized void streamFiles() {
		logger.info("Entering streamFiles()");
		File[] files = dir.listFiles();
		if (files != null) {

			for (int i = 0; i < files.length; i++) {
				new HTTPStreamer(files[i], SessionFactory.getSession());
				files[i].delete();
				logger.info("JVM Memory: " + Runtime.getRuntime().maxMemory());
				logger.info("Free Memory:" + Runtime.getRuntime().freeMemory());
			}
		}
		logger.info("Exiting streamFiles()");

	}

	// Delete directory
	private static boolean deleteDir(String strFile) {
		logger.info("Entering deleteDir()");
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
		logger.info("Exiting deleteDir()");

		// The directory is now empty so delete it
		return fDir.delete();

	}

	private class BufferMonitor extends Thread {

		public void run() {
			try {
				while (true) {
					logger.info("Checking buffer...");
					logger.info("Buffer is "+ buffer.size());
					if (getSize() > 20) {
						logger.info("Flushing Buffer...");
						flushtoFile();
					}else{
					logger.info("Buffer checked, only "+ buffer.size());
					}
					synchronized (this) {
						sleep(500);
					}

				}
			} catch (InterruptedException ie) {
				// stop running
			}

		}
	}

	private class StreamMonitor extends Thread {
		public void run() {

			try {
				while (true) {
					streamFiles();
					synchronized (this) {
						sleep(5000);
					}

				}
			} catch (InterruptedException ie) {
				// stop running
			}
		}
	}

}
