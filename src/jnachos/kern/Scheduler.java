/**  
 *  Copyright (c) 1992-1993 The Regents of the University of California.
 *  All rights reserved.  See copyright.h for copyright notice and limitation 
 *  of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/5/08.
 *  Copyright 2008 Patrick J. McSweeney All rights reserved.
 */
package jnachos.kern;

import java.util.LinkedList;
import java.util.HashMap;

/**
 * Routines to choose the next process to run, and to dispatch to that process.
 * 
 * These routines assume that interrupts are already disabled. If interrupts are
 * disabled, we can assume mutual exclusion (since we are on a uniprocessor).
 * 
 * NOTE: We can't use Locks to provide mutual exclusion here, since if we needed
 * to wait for a lock, and the lock was busy, we would end up calling
 * FindNextToRun(), and that would put us in an infinite loop.
 * 
 * Very simple implementation -- no priorities, straight FIFO. Might need to be
 * improved in later assignments.
 * 
 */
public class Scheduler {
	/**
	 * The list of ready to run process.
	 */
	//Join-Req 2-Save a pointer to the invoking process some where
    //that can be accessed later (new data structure)
    private static HashMap<Integer,NachosProcess> processTable;
	private static LinkedList<NachosProcess> readyList;
	


	/**
	 * Initialize the list of ready but not running process to empty.
	 */
	Scheduler() {
		// Create a list of the processes
		readyList = new LinkedList<NachosProcess>();
		processTable = new HashMap<Integer,NachosProcess>();
	}

	/**
	 * De-allocate the list of ready process.
	 */
	public static void killScheduler() {
		// Iterate through the list of ready Processes
		while (!readyList.isEmpty()) {
			// Remove the next process from the list
			NachosProcess proc = readyList.removeFirst();

			// Kill this process
			proc.kill();
		}

		// Mark the ready list as null
		readyList = null;
	}

	/**
	 * Mark a process as ready, but not running. Put it on the ready list, for
	 * later scheduling onto the CPU.
	 *
	 * @param pProcess
	 *            is the process to be put on the ready list.
	 **/
	public static void readyToRun(NachosProcess pProcess) {
		Debug.print('t', "Putting process " + pProcess.getName() + " on ready list.\n");

		// Mark this process as ready to run
		pProcess.setStatus(ProcessStatus.READY);

		// Add this process to the list of process
		readyList.addLast(pProcess);
	}


/*
    Join Req 5 If so (previous), save the input to Exit (exit status)
    to the waiting process so that it is the return value
    from Join.
*/

    //
	public static boolean checkwaitingproc(int processID, int val)
	{
        if (processTable.containsKey(processID))
        {
        NachosProcess waitingProcess = processTable.remove(processID);
        waitingProcess.saveregvalues(2, val);
        // now the process need to be put back into the queue
            //Join Req 6 Call readyToRun on the waiting process so that it
            //will run again.
        readyToRun(waitingProcess);
        return true;
    }
    else
        {
        return false;
    }

    }




	/**
	 * Return the next process to be scheduled onto the CPU. If there are no
	 * ready processes, return NULL. Side effect: Process is removed from the
	 * ready list.
	 * 
	 * @return The next process that is able to run is returned, null if non.
	 **/
	public static NachosProcess findNextToRun() {
		// If the list is empty return null
		if (readyList.isEmpty())
			return null;

		// Return the head of the list
		return readyList.removeFirst();
	}

	//Join Req 4 Every time a process calls the Exit System call,check if any other process is waiting for it to finish.
    public static void waitingprocessTable(int processID, NachosProcess processwaiting)
    {

        processTable.put(processID, processwaiting);
    }

    /**
     * Print the scheduler state -- in other words, the contents of the ready
     * list. For debugging.
     **/

	public static void Print() {
		System.out.println("Ready list contents:\n");
		for (NachosProcess np : readyList) {
			System.out.println(np);
		}
	}

    // Join-req1 Ensure that the specified process exists.

	public static boolean checkProcess(int processID)
    {
		for (int i = 0; i < readyList.size(); i++)
		{
			NachosProcess nachosProcess = readyList.get(i);
			if (processID == nachosProcess.getpid())
			{
				return true;
			}
		}
		return false;
	}
	

	

}
