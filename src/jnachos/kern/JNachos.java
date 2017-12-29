/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */

package jnachos.kern;

import java.util.*;
import java.util.Hashtable;
import java.util.Enumeration;

import jnachos.machine.*;
import jnachos.filesystem.*;

/**
 * Interrupt handler for the timer device. The timer device is set up to
 * interrupt the CPU periodically (once every TimerTicks). This routine is
 * called each time there is a timer interrupt, with interrupts disabled.
 *
 * Note that instead of calling yield() directly (which would suspend the
 * interrupt handler, not the interrupted process which is what we wanted to
 * context switch), we set a flag so that once the interrupt handler is done, it
 * will appear as if the interrupted thread called Yield at the point it is was
 * interrupted.
 **/
class TimerInterruptHandler implements VoidFunctionPtr {
	/** Default Constructor. */
	TimerInterruptHandler() {
	}

	/**
	 * The call back function. This function is invoked when the timer signals a
	 * timer interrupt.
	 * 
	 * @param pDummy
	 *            is not used.
	 * @see jnachos.machine.Timer
	 **/
	public void call(Object pDummy) {
		// If we are not in idle mode
		if (Interrupt.getStatus() != Interrupt.IdleMode) {
			// Yield on return
			Interrupt.yieldOnReturn();
		}

		Debug.print('i', "Timer Interrupt Handler Being called");
	}
}

/**
 * All global variables used in JNachos are defined here. This class represents
 * the entire operating system. Everything is expected to be static.
 **/
public abstract class JNachos {
	/**
	 * The currently running process. At present 1 CPU = 1 running process.
	 */
	
	static Hashtable<Integer, NachosProcess> mProcessTable = new Hashtable<Integer, NachosProcess>();
	static Hashtable<Integer,Integer > mWaitingTable = new Hashtable<Integer, Integer>();

	private static NachosProcess mCurrentProcess;

	/**
	 * Points to a process that should be destroyed.
	 */
	private static NachosProcess mProcessToBeDestroyed;

	/**
	 * The global scheduler in our system. This class decides which process to
	 * run next.
	 */
	private static Scheduler mScheduler;

	/**
	 * Keeps running statistics of our program.
	 */
	private static Statistics mStats;

	/**
	 * The simulated machine. This class is only used when we are running user
	 * programs.
	 */
	private static Machine mMachine;

	/**
	 * The file system keeps track of files on the simulated disk. There are two
	 * types of file systems. The JavaFileSystem, is a layer between java and
	 * jnachos
	 */
	public static FileSystem mFileSystem;

	/**
	 * A Synchronized Disk. Used for controlled access to the disk.
	 */
	public static SynchDisk mSynchDisk;

	/*
	 * 
	 * private static PostOffice mPostOffice;
	 */

	/**
	 * The constructor for this class should never be called. Every member
	 * variable and function should be static.
	 */
	public JNachos() {
	}

	/**
	 * Initialize JNachos global data structures. Interpret command line
	 * arguments in order to determine flags for the initialization. "argc" is
	 * the number of command line arguments (including the name of the command)
	 * -- ex: "java jnachos/Main -d +" -> argc = 3 "argv" is an array of
	 * strings, one for each command line argument ex: "nachos -d +" -> argv =
	 * {"nachos", "-d", "+"}
	 **/
	public static void initialize(String args[]) {

		int argCount = 0;
		int argv = -1;
		int argc = args.length + 1;
		String debugArgs = new String();
		boolean randomYield = false;

		// single step user program
		boolean debugUserProg = false;

		// format disk
		boolean format = false;

		// network reliability
		double rely = 1;

		// UNIX socket name
		int netname = 0;
		int seed = 0;

		// Run through all of the arguments
		for (argc--, argv++; argc > 0; argc -= argCount, argv += argCount) {
			argCount = 1;
			// System.out.println(argv + "\t" + args[argv].compareTo("-d"));
			if (args[argv].compareTo("-d") == 0) {
				System.out.println("hit");
				if (argc == 1) {
					debugArgs = "+"; // turn on all debug flags
				} else {
					debugArgs = args[argv + 1];
					argCount = 2;
				}

				System.out.println("da: " + debugArgs);
			} else if (args[argv].compareTo("-rs") == 0) {
				assert (argc > 1);
				seed = new Integer(args[argv + 1]);

				// number generator
				randomYield = true;
				argCount = 2;
			}
			// Turn on debugging of user programs
			if (args[argv].compareTo("-s") == 0) {
				debugUserProg = true;
			}
			if (args[argv].compareTo("-f") == 0) {
				format = true;
			}

			if (args[argv].compareTo("-q") == 0) {
				assert (argc >= 1);
				rely = new Float(args[argv + 1]);
				argCount = 2;
			}
			if (args[argv].compareTo("-m") == 0) {
				assert (argc > 1);

				// netname = new Float(args[argv + 1]);
				argCount = 2;
			}
		}

		// initialize DEBUG messages
		Debug.debugInit(debugArgs);

		// Initialize the interrupts
		Interrupt.init();

		// initialize the ready queue
		setScheduler(new Scheduler());

		// Initialize the process to be destroyed
		mProcessToBeDestroyed = null;

		// We didn't explicitly allocate the current thread we are running in.
		// But if it ever tries to give up the CPU, we better have a Thread
		// object to save its state.
		mCurrentProcess = new NachosProcess("Main");
		mCurrentProcess.setAsBootProcess();

		// enable the interrupts
		Interrupt.enable();

		// this must come first
		mMachine = new Machine(debugUserProg, (new TimerInterruptHandler()), seed, randomYield);
		mSynchDisk = new SynchDisk("DISK");

		mFileSystem = new JavaFileSystem(format);

		/*
		 * mPostOffice = new PostOffice(netname, rely, 10);
		 */

	}

	/**
	 * Cleaning up the operating system on shut down. JNachos is halting.
	 * De-allocate global data structures.
	 **/
	public static void cleanUp() {
		Debug.print('n', "\nCleaning up...\n");

		// Exit the program
		System.exit(0);
	}

	/**
	 * Returns the NachosProcess which is currently executing.
	 * 
	 * @return the currently executing jnachos process
	 **/
	public static NachosProcess getCurrentProcess() {
		return mCurrentProcess;
	}

	/**
	 * Updates the currently running process (called on context switch).
	 * 
	 * @param pProcess
	 *            is set as the currently executing jnachos process
	 **/
	public static void setCurrentProcess(NachosProcess pProcess) {
		mCurrentProcess = pProcess;
	}

	/**
	 * Returns the process to be destroyed on the next context switch.
	 * 
	 * @return the jnachos process which needs to be destroyed at the next
	 *         context switch, null if there is no jnachos process.
	 **/
	public static NachosProcess getProcessToBeDestroyed() {
		return mProcessToBeDestroyed;
	}

	/**
	 * Sets the process to be destroyed on the next context switch.
	 * 
	 * @param pProcess
	 *            a jnachos process which needs to be destroyed. Should be the
	 *            current process.
	 **/
	public static void setProcessToBeDestroyed(NachosProcess pProcess) {
		assert ((pProcess == mCurrentProcess) || (pProcess == null));
		mProcessToBeDestroyed = pProcess;
	}

	/**
	 * Sets the cpu scheduler for the OS.
	 * 
	 * @param pScheduler
	 *            the mScheduler to set
	 */
	public static void setScheduler(Scheduler pScheduler) {
		JNachos.mScheduler = pScheduler;
	}

	/**
	 * Gets the cpu scheduler for the OS.
	 * 
	 * @return the Scheduler
	 */
	public static Scheduler getScheduler() {
		return mScheduler;
	}

	/**
	 * Sets the statistics for the OS.
	 * 
	 * @param pStats
	 *            the mStats to set
	 */
	public static void setStats(Statistics pStats) {
		JNachos.mStats = pStats;
	}

	/**
	 * Gets the Statistics object used to track to OS.
	 * 
	 * @return the mStats
	 */
	public static Statistics getStats() {
		return mStats;
	}

	/**
	 * Sets the machine which is simulated hardware.
	 * 
	 * @param mMachine
	 *            the mMachine to set
	 */
	public static void setMachine(Machine pMachine) {
		JNachos.mMachine = pMachine;
	}

	/**
	 * Gets the simulated hardware.
	 * 
	 * @return the mMachine
	 */
	public static Machine getMachine() {
		return mMachine;
	}
	
	 
	
}
