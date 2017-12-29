/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern.sync;

import jnachos.kern.*;
import jnachos.kern.NachosProcess;
import jnachos.machine.Interrupt;
import java.util.LinkedList;

/**
 * A semaphore allows for two processes to synchronize. P() and V() are
 * implemented for suspending and resuming processes.
 */
public class Semaphore {

	/** The name of this semaphore for debugging. */
	private String mName;

	/** The value of this semaphore's token. */
	private int mValue;

	/** The list of sleeping NachosProcesses. */
	private LinkedList<NachosProcess> mQueue;

	/**
	 * Semaphore Constructor creates a semaphore for synchronization.
	 *
	 * @param pDebugName
	 *            The debug name for this semaphore.
	 * @param pInitialValue
	 *            The initial value for the semaphore.
	 */
	public Semaphore(String pDebugName, int pInitialValue) {
		mName = pDebugName;
		mValue = pInitialValue;
		mQueue = new LinkedList<NachosProcess>();
	}

	/**
	 * If we are killing a semaphore, kill all of the sleeping threads.
	 *
	 */
	public void delete() {
		for (NachosProcess p : mQueue) {
			p.kill();
		}
	}

	/**
	 * Returns the name of the semaphore.
	 * 
	 * @return The name of this semaphore.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Wait until semaphore value > 0, then decrement. Checking the value and
	 * decrementing must be done atomically, so we need to disable interrupts
	 * before checking the value.
	 *
	 * Note that NachosProcess::sleep assumes that interrupts are disabled when
	 * it is called.
	 */
	public void P() {
		// disable interrupts
		boolean oldLevel = Interrupt.setLevel(false);

		// Get the current process
		NachosProcess proc = JNachos.getCurrentProcess();

		// decrement the value
		mValue--;

		Debug.print('s', "P: " + proc.getName() + "\t" + mName + "\t" + mValue);

		// If there is not available value
		if (mValue < 0) {
			// Add this process to the queue
			mQueue.addLast(proc);

			Debug.print('s', "P: sleeping " + proc.getName() + "\t" + mName + "\t" + mValue);

			// put the process to sleep
			proc.sleep();
		}

		// re-enable interrupts
		Interrupt.setLevel(oldLevel);
	}

	/**
	 * Increment semaphore value, waking up a waiter if necessary. As with P(),
	 * this operation must be atomic, so we need to disable interrupts.
	 * Scheduler::ReadyToRun() assumes that threads are disabled when it is
	 * called.
	 */
	public void V() {
		boolean oldLevel = Interrupt.setLevel(false);
		NachosProcess c_proc = JNachos.getCurrentProcess();

		// make thread ready, consuming the V immediately
		if (!mQueue.isEmpty()) {
			NachosProcess proc = mQueue.removeFirst();
			Debug.print('s', "Waking: " + mName + proc.getName());
			Scheduler.readyToRun(proc);
		}

		mValue++;

		Debug.print('s', "V: " + c_proc.getName() + "\t" + mName + "\t" + mValue + "\t" + mQueue.size());
		Interrupt.setLevel(oldLevel);
	}
}
