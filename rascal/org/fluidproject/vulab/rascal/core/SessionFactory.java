/*
Copyright 2008 York University

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://source.fluidproject.org/svn/LICENSE.txt

*/
package org.fluidproject.vulab.rascal.core;
import java.util.Date;


public class SessionFactory {

	private static long session = -1;
	
	public static long getSession(){
		return session;
	}
	
	public static void newSession(){
		session = new Date().getTime();
	}
}
