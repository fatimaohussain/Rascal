/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt


Download

*/

/**
 *
 * RASCAL or Remote Activity Screen Capture And Logger is a remote screen recording tool designed to capture
 the mouse, keyboard, screen, and audio of a user.
 *
 * ScreenCastApplet class is an applet through which RASCAL can be embedded into a webpage
   and screen recording can be captured.
 */
package org.fluidproject.vulab.rascal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fluidproject.vulab.rascal.core.AudioRecorder;
import org.fluidproject.vulab.rascal.core.ScreenCapture;
import org.fluidproject.vulab.rascal.core.ScreenRecorder;
import org.fluidproject.vulab.rascal.core.SessionFactory;
import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;




public class ScreencastApplet extends JApplet implements ActionListener, ScreencastJSControl {
	
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -8793677269529774348L;

	private static final String RECORD_BUTTON_BEGIN_STRING = "Start Recording";
	private static final String RECORD_BUTTON_STOP_STRING = "Stop Recording";
	private static final String RECORD_BUTTON_SAVING_STRING = "Saving...";

	private JButton button;
	private JLabel status;

	public void init() {
		logger.info("Entering init()");
		logger.info("Build: RASCAL 0.5B");
		logger.info("JVM Memory: "+Runtime.getRuntime().maxMemory());
		logger.info("Free Memory:"+Runtime.getRuntime().freeMemory());
		status = new JLabel("");
		isRecording = false;

		launchJSMonitor();
		
		/*
                 * Depending on the mode, a GUI is displayed in the applet.
                 * When the 'nogui' mode is used, the GUI is not diplayed.
                 */
		String mode = getParameter("mode");
		logger.info("Mode:"+mode);
		if (mode == null || !mode.equalsIgnoreCase("nogui")){
			logger.info("Start Building GUI");
			button = new JButton(RECORD_BUTTON_BEGIN_STRING);
			button.addActionListener(this);
			JPanel pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
			pane.add(button);
			pane.add(status);
			add(pane);
			logger.info("Finish Building GUI");
		}
		logger.info("Exiting init()");
	}

	private void launchJSMonitor() {
		logger.info("Starting JS Signal Monitor");
		jsm = new Thread(new JSSignalMonitor());
		jsm.start();
		logger.info("JS Signal Monitor launched");
	}

        /**
         * Starts recording the video and audio from the current screen display.
         * @throws IOException
         */
	private void startRecord() throws IOException {
		logger.info("Entering startRecord()");
		isRecording = true;
		SessionFactory.newSession();
		audio = new AudioRecorder();
                video = new ScreenRecorder();
		
		audio.start();
                video.start();
		
		logger.info("Entering startRecord()");
	}

        /**
         * Stops recording the audio and video. This method also ends the file streaming,
         * i.e., files are no longer uploaded to the server.
         *
         */
	private void stopRecord() {
		logger.info("Entering stopRecord()");
		//First of all the screen capture thread and the adio capture is stopped
                video.stopSCapture();
                audio.stop();
                //The writer thread is stopped and the file streaming is finalized.
		video.stop();
                audio.streamAudioFile();
                
                logger.info("Everything stopped");

                new HTTPStreamer().endStreaming(SessionFactory.getSession());
		isRecording = false;
		logger.info("Exiting stopRecord()");
	}

        //Screen Recording Thread
	ScreenRecorder video;
        //Audio Recording Thread
	AudioRecorder audio;
	private boolean isRecording;
	private String jsSignal = "";
	
	public synchronized String getJsSignal() {
		return jsSignal;
	}

	public synchronized void setJsSignal(String signal) {
		this.jsSignal = signal;
	}

	Thread sdr, jsm;

	private synchronized void setMessage(String text) {
		logger.info("Entering setMessage()");
		status.setText(text);
		repaint();
		logger.info("Exiting setMessage()");
	}

	public void actionPerformed(ActionEvent e) {
		logger.info("Entering actionPerformed()");
		if (isRecording) {
			logger.info("isRecording = true");
			setMessage(RECORD_BUTTON_SAVING_STRING);
			button.setEnabled(false);
			sdr = new Thread(new SaveDataRunner());
			sdr.start();
			button.setText(RECORD_BUTTON_BEGIN_STRING);

		} else {
			logger.info("isRecording = false");
			button.setEnabled(false);
			try {
				startRecord();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			button.setText(RECORD_BUTTON_STOP_STRING);
			button.setEnabled(true);
		}
		repaint();
		logger.info("Exiting actionPerformed()");
	}

        /**
         * This class stops the recording and saved the data.
         * This implements Runnable interface and runs as a separate thread.
         *
         */
	private class SaveDataRunner implements Runnable {

		public void run() {
			stopRecord();
			button.setEnabled(true);
			setMessage("Saved: Video id is: " + getSessionid());
		}

	}
	
	private class JSSignalMonitor extends Thread {
		
		public void run(){
			
			while(true){
				synchronized (jsSignal) {
					if (jsSignal.equals("start")){
						logger.info("start js signal received");
						try {
							startRecord();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						jsSignal = "";
					}
					else if (jsSignal.equals("stop")){
						logger.info("stop js signal received");
						stopRecord();
						jsSignal = "";
					}
					try {
                                            //logger.info("js sleeping");
						sleep(5);
					} catch (InterruptedException e) {
						//Time to exit
						e.printStackTrace();
					}
				}
			}
		}
	}

	public long getSessionid() {
		return SessionFactory.getSession();
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void startRecording() {
		while (!jsSignal.equals(""));
		jsSignal = "start";
	}

	public void stopRecording() {
		while (!jsSignal.equals(""));
		jsSignal = "stop";
		
	}


}
