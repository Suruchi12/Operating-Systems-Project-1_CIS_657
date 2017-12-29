/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

/**
 * Set up a ping-pong between two threads, by forking a thread to 'call'.
 **/
public class ProcessTest implements VoidFunctionPtr {
	/**
	 * The body for processes to run
	 * 
	 * @param pArg
	 *            is an Integer with an id.
	 */
	public void call(Object pArg) {
		// which is the processes local id
		int which = ((Integer) pArg).intValue();

		// Loop 5 times
		for (int num = 0; num < 5; num++) {
			System.out.println("*** Process " + which + " looped " + num + " times.");
			JNachos.getCurrentProcess().yield();
		}
	}

	/**
	 * ThreadTest Set up a ping-pong between two threads, by forking a thread to
	 * call SimpleThread, and then calling SimpleThread ourselves.
	 **/
	public ProcessTest() {
		Debug.print('t', "Entering SimpleTest");

		// Fork off 5 threads to this class.
		for (int i = 0; i < 5; i++) {
			NachosProcess p = new NachosProcess("forked process" + i);
			p.fork(this, new Integer(i));
		}
	}
}
