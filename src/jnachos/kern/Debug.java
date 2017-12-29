/** 
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/6/08.
 **/
package jnachos.kern;

/**
 * Miscellaneous useful definitions, including debugging routines.
 *
 * The debugging routines allow the user to turn on selected debugging messages,
 * controllable from the command line arguments passed to Nachos (-d). You are
 * encouraged to add your own debugging flags. The pre-defined debugging flags
 * are:
 *
 * '+' -- turn on all debug messages 't' -- thread system 's' -- semaphores,
 * locks, and conditions 'i' -- interrupt emulation 'm' -- machine emulation
 * (USER_PROGRAM) 'd' -- disk emulation (FILESYS) 'f' -- file system (FILESYS)
 * 'a' -- address spaces (USER_PROGRAM) 'n' -- network emulation (NETWORK)
 */
public abstract class Debug {
	/**
	 * The set of flags used for debugging.
	 */
	private static String mFlags;

	/**
	 * Initializes the Debugging in Nachos.
	 * 
	 * @param pDebugArgs
	 *            the set of flags for debugging
	 */
	public static void debugInit(String pDebugArgs) {
		mFlags = pDebugArgs;
	}

	/**
	 * A conditional print of a debug message.
	 * 
	 * @param pFlag
	 *            The conditional debug flag
	 * @param pMessage
	 *            the message to display if the flag is active.
	 **/
	public static void print(char pFlag, String pMessage) {
		// Check if the flag is active
		if (isEnabled(pFlag)) {
			System.out.println(pMessage);
		}
	}

	/**
	 * Checks to see if a given debug flag is enabled.
	 * 
	 * @param pFlag
	 *            the flag to check.
	 **/
	public static boolean isEnabled(char pFlag) {
		// The character arrays of active flags
		char[] array = mFlags.toCharArray();

		// Check each character
		for (int i = 0; i < array.length; i++) {
			// if the flag was found return true
			if (array[i] == pFlag) {
				return true;
			}

			// If all flags have been turned on
			else if (array[i] == '+') {
				return true;
			}
		}

		// The flag was not found
		return false;
	}
}
