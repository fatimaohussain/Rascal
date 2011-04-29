/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/

package org.fluidproject.vulab.rascal.ui;

public interface ScreencastJSControl {

	public void startRecording();
	
	public void stopRecording();
	
	public long getSessionid();
	
	public boolean isRecording();
	
	
}
