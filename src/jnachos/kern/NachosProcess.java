/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/5/08.
 *  Copyright 2008 Patrick J. McSweeney All rights reserved.
 */
package jnachos.kern;

import jnachos.machine.*;
import java.lang.*;

/**
 * Process states in JNachos. JUST_CREATED: The NachosProcess was just created.
 * RUNNING: The NachosProcess is the current process. READY: The NachosProcess
 * is waiting to run. KILLED: The NachosProcess has finished and should be
 * killed.
 */
enum ProcessStatus {
	JUST_CREATED, RUNNING, READY, BLOCKED, KILLED
};

/**
 * Routines to manage NachosProcesses. There are four main operations:
 *
 * fork: create a nachos process to run a procedure concurrently with the caller
 * (this is done in two steps -- first allocate the process object, then call
 * Fork on it) finish: called when the forked procedure finishes, to clean up
 * yield: relinquish control over the CPU to another ready process. sleep:
 * relinquish control over the CPU, but the process is now blocked. In other
 * words, it will not run again, until explicitly put back on the ready queue.
 *
 **/
public class NachosProcess implements Runnable {
    int processid;
    /**
	 * A static boolean ensures setAsBootProcess is only called once/
	 */

	private static boolean booted = false;

	/**
	 * The status of the process.
	 */
	private ProcessStatus mStatus;

	/**
	 * The name of the process.
	 */
	private String mName;

	/**
	 * These variables are only for the user-level CPU register state.
	 */
	private int[] mUserRegisters;
	/**
	 * User code this Process is running.
	 */
	private AddrSpace mSpace;

	/**
	 * The function to for this process to run.
	 */
	private VoidFunctionPtr myFunc;

	/**
	 * The parameter to the function.
	 */
	private Object myArg;

	/**
	 * The Java-level thread that does the work.
	 */
	private Thread mThread;

	/**
	 * Indicates whether or not this process has already been started.
	 */
	private boolean mStarted;



    /**
	 * Initialize a Process control block, so that we can then call fork.
	 * 
	 * @param ProcessName
	 *            is an arbitrary string, useful for debugging.
	 **/
	
	// to generate process id uniquely
    private  static int set_pid=0;

    public static void setSet_pid(int set_pid)
    {
        NachosProcess.set_pid = set_pid;
    }
	
    
	public NachosProcess(String pProcessName) {
		// Save the process name
		mName = pProcessName;

		// Set the process status
		mStatus = ProcessStatus.JUST_CREATED;

		// Initialize some of the pointers
		mSpace = null;
		mStarted = false;

		mUserRegisters = new int[Machine.NumTotalRegs];

		   //get a different process id
		
		processid = set_pid++;
	}

	/**
	 * Suspend this NachosProcesses.
	 *
	 **/
	public void suspend() {
		Object o = new Object();
		try {
			this.wait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Can be called only once in the beginnign of the process.
	 *
	 **/
	public void setAsBootProcess() {
		// Assert this function has not yet been called
		assert (!booted);

		// Set this process as running
		this.setStatus(ProcessStatus.RUNNING);

		// Set the thread as the current Java thread
		mThread = Thread.currentThread();

		// This process has started
		mStarted = true;

		// Remember that we have booted
		booted = true;
	}

	/**
	 * Resumes this process. NOTE: We piggy back Nachos Processes ontop of java
	 * level threads. So if this is the first time the thread is running we have
	 * to start the java thread otherwise we resume it.
	 **/
	public synchronized void resume() {
		// If this process has already begun, simply resume it
		if (mStarted) {
			this.notify();
		} else {
			// If this thread has not yet run, start it
			mThread.start();

			// Remember that we have started it
			mStarted = true;

		}
	}

	/**
	 * The main function for the Java-level thread
	 **/
	public void run() {
		// Before we start running check to see if there is a thread
		// that needs to be destroyed
		if (JNachos.getProcessToBeDestroyed() != null) {
			JNachos.getProcessToBeDestroyed().kill();
			JNachos.setProcessToBeDestroyed(null);
		}

		// Make sure that when we start to run a new proceess
		// interrupts should be turned ON!!!
		Interrupt.setLevel(true);
		myFunc.call(myArg);

		// Finish this process when its over.
		finish();
	}

	/**
	 * De-allocate a Process. NOTE: the current Process *cannot* delete itself
	 * directly, since it is still running on the stack that we need to delete.
	 *
	 * NOTE: if this is the main Process, we can't delete the stack because we
	 * didn't allocate it -- we got it automatically as part of starting up
	 * Nachos.
	 **/
	public void kill() {
		Debug.print('t', "Deleting Process " + mName);
		assert (this != JNachos.getCurrentProcess());
		if (mSpace != null) {

		}
	}

	/**
	 * Invoke VoidFunctionPtr.call, allowing caller and callee to execute
	 * concurrently.
	 *
	 * NOTE: although our definition allows only a single integer argument to be
	 * passed to the procedure, it is possible to pass multiple arguments by
	 * making them fields of a class, and passing an object to the structure as
	 * "arg".
	 *
	 * 
	 * @param pFunc
	 *            is the object that has the call function.
	 * @param pArg
	 *            is a single argument to be passed to the procedure.
	 **/
	public void fork(VoidFunctionPtr pFunc, Object pArg) {
		Debug.print('t', "Forking Process " + mName + "with func = " + pFunc + ", arg = " + pArg);

		// Capture the current state of the interrupts
		boolean oldLevel = Interrupt.setLevel(false);

		// save the parameters in this process object
		myFunc = pFunc;
		myArg = pArg;

		// Create a new thread for this process
		mThread = new Thread(this);

		// ReadyToRun assumes that interrupts are disabled!
		Scheduler.readyToRun(this);

		// return interrupts to their pre-call levels
		Interrupt.setLevel(oldLevel);
	}

	/**
	 * Called when a Process is done executing the forked procedure.
	 *
	 * NOTE: We can't kill a process while its running. Instead, we set
	 * "ProcessToBeDestroyed", so that Scheduler will call the destructor, once
	 * we're running in the context of a different Process.
	 *
	 * NOTE: We disable interrupts, so that we don't get a time slice between
	 * setting ProcessToBeDestroyed, and going to sleep.
	 **/
	public void finish() {
		// Turn off interrupts
		Interrupt.setLevel(false);

		// Processes can only kill themselves
		assert (this == JNachos.getCurrentProcess());

		Debug.print('t', "Finishing Process " + getName());

		// Mark this process as to be destroyed
		JNachos.setProcessToBeDestroyed(this);

		// Put ourself to sleep
		sleep();
	}

	/**
	 * Relinquish the CPU if any other Process is ready to run. If so, put the
	 * Process on the end of the ready list, so that it will eventually be
	 * re-scheduled.
	 *
	 * NOTE: Returns immediately if no other Process on the ready queue.
	 * Otherwise returns when the Process eventually works its way to the front
	 * of the ready list and gets re-scheduled.
	 *
	 * NOTE: We disable interrupts, so that looking at the Process on the front
	 * of the ready list, and switching to it, can be done atomically. On
	 * return, we re-set the interrupt level to its original state, in case we
	 * are called with interrupts disabled.
	 *
	 * Similar to sleep, but a little different.
	 **/
	public void yield() {
		NachosProcess nextProcess;

		// Turn off interrupts
		boolean oldLevel = Interrupt.setLevel(false);

		// Only the currently executing process can yield
		assert (this == JNachos.getCurrentProcess());

		Debug.print('t', "Yielding Process " + getName());

		// Find the next process to run
		nextProcess = Scheduler.findNextToRun();

		// If there is a process
		if (nextProcess != null) {
			// Mark this process as ready
			Scheduler.readyToRun(this);

			// Run the other process
			JNachos.getCurrentProcess().switchProcess(nextProcess);
		}

		// Return interrupts to their pre call level
		Interrupt.setLevel(oldLevel);
	}

	/**
	 * Relinquish the CPU, because the current Process is blocked waiting on a
	 * synchronization variable (Semaphore, Lock, or Condition). Eventually,
	 * some Process will wake this Process up, and put it back on the ready
	 * queue, so that it can be re-scheduled.
	 *
	 * NOTE: If there are no Processs on the ready queue, that means we have no
	 * Process to run. "Interrupt::Idle" is called to signify that we should
	 * idle the CPU until the next I/O interrupt occurs (the only thing that
	 * could cause a Process to become ready to run).
	 *
	 * NOTE: we assume interrupts are already disabled, because it is called
	 * from the synchronization routines which must disable interrupts for
	 * atomicity. We need interrupts off so that there can't be a time slice
	 * between pulling the first Process off the ready list, and switching to
	 * it.
	 **/
	public void sleep() {
		NachosProcess nextProcess;

		// Only the currently executing process can sleep
		assert (this == JNachos.getCurrentProcess());

		// interrupts should already be disabled
		assert (Interrupt.getLevel() == false);

		Debug.print('t', "Sleeping Process" + getName());

		// Set the status for this process to blocked
		mStatus = ProcessStatus.BLOCKED;

		// no one to run, wait for an interrupt
		while ((nextProcess = Scheduler.findNextToRun()) == null) {
			Interrupt.idle();
		}

		// returns when we've been signalled
		JNachos.getCurrentProcess().switchProcess(nextProcess);
	}

	/**
	 * Dispatch the CPU to nextProcess. Save the state of the old process, and
	 * load the state of the new process, by calling the machine dependent
	 * context switch routine, SWITCH.
	 *
	 * Note: we assume the state of the previously running process has already
	 * been changed from running to blocked or ready (depending). Side effect:
	 * The global variable currentProccess becomes nextProcess.
	 *
	 * @param pNextProcess
	 *            is the process to be put into the CPU.
	 **/
	public synchronized void switchProcess(NachosProcess pNextProcess) {
		// Get the current process
		NachosProcess oldProcess = JNachos.getCurrentProcess();

		if (oldProcess == pNextProcess)
			return;

		// If this process's address space is not null
		if (oldProcess.getSpace() != null) {
			// save the user's CPU registers
			oldProcess.saveUserState();

			// save the address's space state
			oldProcess.getSpace().saveState();
		}

		// switch to the next process
		JNachos.setCurrentProcess(pNextProcess);

		// nextProcess is now running
		pNextProcess.setStatus(ProcessStatus.RUNNING);

		Debug.print('t', "Switching from process " + oldProcess.getName() + " to process " + pNextProcess.getName());

		// Resume the other process
		pNextProcess.resume();

		// Stop the current process
		oldProcess.suspend();

		Debug.print('t', "Now in process " + pNextProcess.getName());

		// If the old process gave up the processor because it was finishing,
		// we need to delete its carcass. Note we cannot delete the process
		// before now (for example, in finish()), because up to this
		// point, we were still running on the old process's stack!
		if (JNachos.getProcessToBeDestroyed() != null) {
			JNachos.getProcessToBeDestroyed().kill();
			JNachos.setProcessToBeDestroyed(null);
		}

		if (oldProcess.getSpace() != null) {
			oldProcess.restoreUserState();
			oldProcess.getSpace().restoreState();
		}
	}

	/**
	 * Save the CPU state of a user program on a context switch.
	 *
	 **/
	public void saveUserState() {
		for (int i = 0; i < Machine.NumTotalRegs; i++) {
			mUserRegisters[i] = Machine.readRegister(i);
		}
	}

	
	/**
	 * Restore the CPU state of a user program on a context switch.
	 *
	 **/
	public void restoreUserState() {
		for (int i = 0; i < Machine.NumTotalRegs; i++) {
			Machine.writeRegister(i, mUserRegisters[i]);
		}
	}

	/**
	 * Sets the address space for this process.
	 * 
	 * @param Sets
	 *            the address space for this process
	 *
	 **/
	public void setSpace(AddrSpace pAddr) {
		mSpace = pAddr;
	}

	/**
	 * Gets the address space for this process.
	 * 
	 * @return the address for this process
	 **/
	public AddrSpace getSpace() {
		return mSpace;
	}

	/**
	 * Gets the status for this process.
	 * 
	 * @return the status of this process
	 *
	 **/

    public void saveregvalues(int num, int value) {

        if ((num >= 0) && (num < Machine.NumTotalRegs))
        {
            mUserRegisters[num] = value;
        }
        Debug.print('m', "Save User Register " + num + ", value" + value);
    }
	public ProcessStatus getStatus() {
		return mStatus;
	}

	/**
	 * Sets the status of the process.
	 * 
	 * @param pStatus
	 *            Sets the status of this process to the parameter
	 **/
	public void setStatus(ProcessStatus pStatus) {
		mStatus = pStatus;
	}

	/**
	 * Gets the name for this process.
	 * 
	 * @return String the name for this thread.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Gets a string representing this process.
	 * 
	 * @return The display name for this process
	 **/
	public String toString() {
		return new String(mName + "\t" + getStatus());
	}
	
	// return the unique process id
    	public int getpid()
	{

	    return processid;
	}


}
