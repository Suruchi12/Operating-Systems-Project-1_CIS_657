/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

/**
 *
 */
public class Statistics {

	// Constants used to reflect the relative time an operation would
	// take in a real system. A "tick" is a just a unit of time -- if you
	// like, a microsecond.
	//
	// Since Nachos kernel code is directly executed, and the time spent
	// in the kernel measured by the number of calls to enable interrupts,
	// these time constants are none too exact.

	public static int UserTick = 1; // advance for each user-level instruction
	public static int SystemTick = 10; // advance each time interrupts are
										// enabled
	public static int RotationTime = 500; // time disk takes to rotate one
											// sector
	public static int SeekTime = 500; // time disk takes to seek past one track
	public static int ConsoleTime = 100; // time to read or write one character
	public static int NetworkTime = 100; // time to send or receive one packet
	public static int TimerTicks = 100; // (average) time between timer
										// interrupts

	// The following class defines the statistics that are to be kept
	// about Nachos behavior -- how much time (ticks) elapsed, how
	// many user instructions executed, etc.
	//
	// The fields in this class are public to make it easier to update.

	public static int totalTicks; // Total time running Nachos
	public static int idleTicks; // Time spent idle (no threads to run)
	public static int systemTicks; // Time spent executing system code
	public static int userTicks; // Time spent executing user code
	// (this is also equal to # of
	// user instructions executed)

	public static int numDiskReads; // number of disk read requests
	public static int numDiskWrites; // number of disk write requests
	public static int numConsoleCharsRead; // number of characters read from the
											// keyboard
	public static int numConsoleCharsWritten; // number of characters written to
												// the display
	public static int numPageFaults; // number of virtual memory page faults
	public static int numPacketsSent; // number of packets sent over the network
	public static int numPacketsRecvd; // number of packets received over the
										// network

	/**
	 * Initialize performance metrics to zero, at system startup.
	 */
	public Statistics() {
		totalTicks = idleTicks = systemTicks = userTicks = 0;
		numDiskReads = numDiskWrites = 0;
		numConsoleCharsRead = numConsoleCharsWritten = 0;
		numPageFaults = numPacketsSent = numPacketsRecvd = 0;
	}

	/**
	 * Print performance metrics, when we've finished everything at system
	 * shutdown.
	 */
	public static void Print() {
		System.out.println("Ticks: total " + totalTicks + ", idle " + idleTicks + ", system " + systemTicks + ", user "
				+ userTicks);

		System.out.println("Disk I/O: reads " + numDiskReads + ", writes " + numDiskWrites);
		System.out.println("Console I/O: reads " + numConsoleCharsRead + ", writes " + numConsoleCharsWritten);
		System.out.println("Paging: faults " + numPageFaults);

		System.out.println("Network I/O: packets received " + numPacketsRecvd + ", sent " + numPacketsSent);
	}

}
