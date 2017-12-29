/*****************************************************************************************
 *							ORIGINAL nachos 3.0 heading									 *
 *---------------------------------------------------------------------------------------*
 *  main.cc																				 *
 *	Bootstrap code to initialize the operating system kernel.							 *	
 *																						 *	
 *	Allows direct calls into internal operating system functions,						 *
 *	to simplify debugging and testing.  In practice, the								 *
 *	bootstrap code would just initialize data structures,								 *
 *	and start a user program to print the login prompt.									 *
 *																						 *
 * 	Most of this file is not needed until later assignments.							 *
 *																						 *
 * Usage: nachos -d <debugflags> -rs <random seed #>									 *
 *		-s -x <nachos file> -c <consoleIn> <consoleOut>									 *
 *		-f -cp <unix file> <nachos file>												 *
 *		-p <nachos file> -r <nachos file> -l -D -t										 *
 *              -n <network reliability> -m <machine id>								 *
 *              -o <other machine id>													 *
 *              -z																		 *
 *																						 *
 *    -d causes certain debugging messages to be printed (cf. utility.h)				 *
 *    -rs causes Yield to occur at random (but repeatable) spots						 *
 *    -z prints the copyright message													 *
 *																					     *
 *  USER_PROGRAM																		 *
 *    -s causes user programs to be executed in single-step mode						 *
 *    -x runs a user program															 *
 *    -c tests the console																 *
 *																						 *
 *  FILESYS																				 *
 *    -f causes the physical disk to be formatted										 *
 *    -cp copies a file from UNIX to Nachos												 *
 *    -p prints a Nachos file to stdout													 *
 *    -r removes a Nachos file from the file system										 *
 *    -l lists the contents of the Nachos directory										 *
 *    -D prints the contents of the entire file system									 *
 *    -t tests the performance of the Nachos file system								 *
 *																						 *
 *  NETWORK																				 *
 *    -n sets the network reliability													 *
 *    -m sets this machine's host id (needed for the network)							 *
 *    -o runs a simple test of the Nachos network software								 *
 *																						 *
 *  NOTE -- flags are ignored until the relevant assignment.							 *
 *  Some of the flags are interpreted here; some in system.cc.							 *
 *																						 *
 * Copyright (c) 1992-1993 The Regents of the University of California.					 *	
 * All rights reserved.  See copyright.h for copyright notice and limitation			 *
 * of liability and disclaimer of warranty provisions.									 *
 *																						 *
 *---------------------------------------------------------------------------------------*
 *								JNachos Heading											 *
 *  Main.java - The java entry point for JNachos										 *
 *  Created by Patrick McSweeney on 12/5/08.											 *
 *	pjmcswee@syr.edu																	 *
 *  Copyright 2008 P.J. McSweeney All rights reserved.									 *
 *****************************************************************************************/
package jnachos;

import jnachos.kern.*;
import jnachos.filesystem.*;
import jnachos.machine.*;

public class Main {

	private static String brief_copyright = new String(
			"Copyright (c) 1992-1993 The Regents of the University of California.");
	private static String copyright = new String(
			"Copyright (c) 1992-1993 The Regents of the University of California.\n"
					+ "All rights reserved.\n Permission to use, copy, modify, and distribute this software and its"
					+ "documentation for any purpose, without fee, and without written agreement is"
					+ "hereby granted, provided that the above copyright notice and the following"
					+ "two paragraphs appear in all copies of this software.\n"
					+ "IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR"
					+ "DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT"
					+ "OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF"
					+ "CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n"
					+ "THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,"
					+ "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY"
					+ "AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS"
					+ "ON AN 'AS IS' BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO"
					+ "PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.");

	/**
	 * main Bootstrap the operating system kernel.
	 * 
	 * Check command line arguments Initialize data structures (optionally) Call
	 * test procedure
	 *
	 * "argc" is the number of command line arguments (including the name of the
	 * command) -- ex: "nachos -d +" -> argc = 3 "argv" is an array of strings,
	 * one for each command line argument ex: "nachos -d +" -> argv = {"nachos",
	 * "-d", "+"}
	 */
	public static void main(String args[]) {
		// The keep track of the number of arguments
		int argCount;

		// Initialize Nachos dataStructures
		JNachos.initialize(args);

		Debug.print('t', "Entering main");

		display();

		// new Water();
		// new ProcessTest();

		// The number arguments
		int argc = args.length;

		// The current argument
		int argv = -1;

		// Process the arguments
		for (argc--, argv++; argc > 0; argc -= argCount, argv += argCount) {

			argCount = 1;

			// Print out the copyright statement
			if (args[argv].compareTo("-z") == 0) {
				System.out.println(copyright);
			}
			// Run a user program
			else if (args[argv].compareTo("-x") == 0) {
				System.out.println(args[argv + 1]);
				assert (argc > 0);
				// Start a process
				String[] programs = args[argv + 1].split(",");
				for(String program : programs) {
					NachosProcess p = new NachosProcess(program);
					p.fork(new StartProcess(), program);
				}
				argCount = 2;
			}
			// Run a console
			else if (args[argv].compareTo("-c") == 0) {
				if (argc == 1) {
					// ConsoleTest(NULL, NULL);
				} else {
					assert (argc > 2);
					// ConsoleTest(*(argv + 1), *(argv + 2)); ?
					argCount = 3;
				}

				// once we start the console, then
				// Nachos will loop forever waiting
				// for console input
				Interrupt.halt();
			}

			// Copy a file from the "real-world" to the Nachos world
			if (args[argv].compareTo("-cp") == 0) {
				assert (argc >= 2);
				FileSystemUtility.copy(args[argv + 1], args[argv + 2]);
				argCount = 3;
			}

			// Print out a Nachos world file
			else if (args[argv].compareTo("-p") == 0) {
				assert (argc >= 1);
				FileSystemUtility.print(args[argv + 1]);
				argCount = 2;
			}
			// remove Nachos World file
			else if (args[argv].compareTo("-r") == 0) {
				assert (argc >= 1);
				JNachos.mFileSystem.remove(args[argv + 1]);
				argCount = 2;
			}
			// List Nachos directory
			else if (args[argv].compareTo("-l") == 0) // list Nachos directory
			{
				JNachos.mFileSystem.list();
			}
			// Print the nachos file system
			else if (args[argv].compareTo("-D") == 0) // print entire filesystem
			{
				JNachos.mFileSystem.print();
			}
			// Run the performance test
			else if (args[argv].compareTo("-t") == 0) // performance test
			{
				// PerformanceTest();
			}

			if (args[argv].compareTo("-o") == 0) {
				assert (argc > 1);
				// Delay(2); // delay for 2 seconds
				// to give the user time to
				// start up another nachos
				// MailTest(new Integer(args[argv + 1]));
				argCount = 2;
			}

		}

		// NOTE: if the procedure "main"
		// returns, then the program "nachos"
		// will exit (as any other normal program
		// would). But there may be other
		// Processs on the ready list. We switch
		// to those Processs by saying that the
		// "main" Process is finished, preventing
		// it from returning.
		JNachos.getCurrentProcess().finish();

		assert (false);

		return; // Not reached...
	}

	/**
	 * Displays the JNachos header.
	 */
	public static void display() {
		System.out.println(brief_copyright);
		System.out.println("Entering JNachos v1.0");
	}

}
