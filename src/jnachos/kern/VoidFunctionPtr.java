/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

/**
 * As java does function pointers, we use this interface to simulate the
 * function pointers. A class implements this interface in order to allow the
 * function call to be called on a "call back" nature.
 **/
public interface VoidFunctionPtr {
	/**
	 * The function which is called as a funciton pointer. Enclosing the
	 * function in a class allows us to "pass" around the function.
	 *
	 * @param pArg
	 *            the argument for this function
	 **/
	public void call(Object pArg);
}
