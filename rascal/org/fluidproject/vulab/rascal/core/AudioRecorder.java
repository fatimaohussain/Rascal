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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.fluidproject.vulab.rascal.ui.ScreencastApplet;
import org.fluidproject.vulab.rascal.utils.http.HTTPStreamer;
import org.fluidproject.vulab.rascal.core.utils.FastFrameBuffer;



public class AudioRecorder implements IConstants {
	public static Logger logger = Logger.getLogger(ScreencastApplet.class.getName());

	
	public static final String AUDIO_FILE_NAME=IConstants.TEMPORARY_AUDIO_FILENAME;
	private AudioFormat audioFormat;
	private TargetDataLine targetDataLine;
	private Thread recorderThread;
	File audioFile = null;

	public void start() {
		logger.info("Entering Audio Record start()");
                File dir = new File (ScreenRecorder.AUDIO_DIRNAME);
                FastFrameBuffer.deleteDir(dir.getPath());
		dir.mkdir();
		/* For testing purposes
		 System.out.println("Audio Recorder: " + AUDIO_FILE_NAME);
		 */
		recorderThread.start();
		logger.info("Exiting Audio Record start()");

	}

        /*
         * This method stops audio recording.
         */

	public void stop() {
		logger.info("Entering Audio Record stop()");
		targetDataLine.close();
		logger.info("line closed");
                
                logger.info("Exiting  Audio Record stop()");
	}

        /**
         *
         * A method that streams audio file after the recording is completed.
         * The audio file is deleted from the client machine afterward.
         */
        public void streamAudioFile(){
            logger.info("Streaming Audio File");
		new HTTPStreamer(audioFile,SessionFactory.getSession());
		logger.info("Deleting Audio file");
		audioFile.delete();
		logger.info("Finishing  Audio Streaming()");

        }

	public AudioRecorder() {// constructor
		// Get things set up for capture
		logger.info("Entering AudioRecorder()");
		recorderThread = new CaptureAudioThread();
		audioFormat = getAudioFormat();
		DataLine.Info dataLineInfo = null;
		
		try{
			dataLineInfo = new DataLine.Info(TargetDataLine.class,
					audioFormat);
			logger.info("Little Endian machine detected");
		}catch(IllegalArgumentException e){
			logger.info("Big Endian machine detected");
			dataLineInfo = new DataLine.Info(TargetDataLine.class,
					getBigEndianAudioFormat());
		}
		
		
		try {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
		}
		logger.info("Exiting AudioRecorder()");

	}// end constructor

	

	private AudioFormat getAudioFormat() {

		return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS,
				SIGNED, BIG_ENDIAN);
	}
	
	private AudioFormat getBigEndianAudioFormat() {
		return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS,
				SIGNED, true);
	}
	

	class CaptureAudioThread extends Thread {
		public void run() {
			AudioFileFormat.Type fileType = null;
			

			fileType = AudioFileFormat.Type.WAVE;
			audioFile = new File(AUDIO_FILE_NAME);
			try {
			
                            logger.info("Creating new Audio file");
                            audioFile.createNewFile();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				targetDataLine.open(audioFormat);
                                logger.info("target Data line opened");
			} catch (LineUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			targetDataLine.start();
			try {
			
                            logger.info("Writing Audio Stream");
                            AudioSystem.write(new AudioInputStream(targetDataLine),
						fileType, audioFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
