package com.developi.util;

public class Utils {

	
	/**
	 * recycles a domino document instance
	 * 
	 * @param lotus.domino.Base 
	 *           obj to recycle
	 */
	public static void recycleObject(lotus.domino.Base obj) {
		if (obj != null) {
			try {
				obj.recycle();
			} catch (Exception e) {}
		}
	}

	/**
	 * 	 recycles multiple domino objects (thx Nathan T. Freeman)
	 *		
	 * @param objs
	 * 
	 */
	public static void recycleObjects(lotus.domino.Base... objs) {
		for ( lotus.domino.Base obj : objs ) 
			recycleObject(obj);
	}

	
	
}
