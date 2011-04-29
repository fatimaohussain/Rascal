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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ImagePostProcessing {

	
	private static long lastNormalized = -1;
	
	public static Hashtable<String, BufferedImage> normalizeTable(Hashtable<String, BufferedImage> table, int intervalInMs){
		Hashtable<String, BufferedImage> result = new Hashtable<String, BufferedImage>(table.size());
		
		List<String> keys = enumToList(table.keys());
		long normKey = -1;
		BufferedImage img = null;
		for (int i = 0; i < keys.size(); i++){
			String keyAsLong = keys.get(i).split(".")[0];
			
			if (i == 0){
				//normalize with lastNormalized
				if (lastNormalized == -1){
					lastNormalized = Long.parseLong(keyAsLong);
				}
			} else{
				//normalize against prev. one
			}
			if (i == keys.size() -1){
				//lastNormalized = last normalized index
			}
		}
		
		String key = normKey +".jpg";
		result.put(key, img);
		
		return result;
	}
	
	private static List<String> enumToList(Enumeration<String> keys) {
		List<String> result = new ArrayList<String>();
		
		while( keys.hasMoreElements()){
			String str = keys.nextElement();
			//str = str.split(".")[0];
			result.add(str);
		}
		Collections.sort(result);
		return result;
	}

	private static BufferedImage lastWritten;
	
	public static boolean delta(BufferedImage img){
		boolean result = lastWritten == null;
		result &= isChanged(lastWritten, img);
		if (result){
			lastWritten = img;
		}
		return result;
	}
	
	private static boolean isChanged(BufferedImage img1, BufferedImage img2){
		
		return false;
	}
}
