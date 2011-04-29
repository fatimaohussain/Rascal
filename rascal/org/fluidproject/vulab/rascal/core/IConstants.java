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


public interface IConstants {

	// audio settings
	
	public static final float SAMPLE_RATE = 8000.0F;
	// 8000,11025,16000,22050,44100
	
	public static final int SAMPLE_SIZE_IN_BITS = 16;
	// 8,16
	
	public static final int CHANNELS = 1;
	// 1,2
	
	public static final boolean SIGNED = true;
	// true,false
	
	public static final boolean BIG_ENDIAN = false;
	// true,false
	
	//public static final String TEMPORARY_AUDIO_FILENAME = ScreenRecorder.DIRNAME + File.separator +  "Audio" + File.separator + "output.wav";
        public static final String TEMPORARY_AUDIO_FILENAME = ScreenRecorder.AUDIO_DIRNAME + File.separator +   "output.wav";
	
}
