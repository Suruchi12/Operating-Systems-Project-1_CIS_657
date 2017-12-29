/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

import jnachos.kern.*;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Pending Interrupt describes an interrupt that is scheduled to occur at some
 * time in the future
 */
class PendingInterrupt {

	/** The function (in the hardware device). */
	private VoidFunctionPtr mHandler;

	/** The argument to the function. */
	private Object mArg;

	/** When the interrupt is supposed to fire. */
	private int mWhen;

	/** Used for Debug printing */
	private InterruptType mType;

	/**
	 * Default Constructor.
	 * 
	 * @param pFunc
	 *            The call-back function
	 * @param pArg
	 *            The argument to the call-back function.
	 * @param pTime
	 *            The time that the interrupt should fire.
	 * @param pKind
	 *            The kind of interrupt.
	 */
	public PendingInterrupt(VoidFunctionPtr pFunc, Object pArg, int pTime, InterruptType pKind) {
		mHandler = pFunc;
		mArg = pArg;
		mWhen = pTime;
		mType = pKind;
	}

	/**
	 * Gets the interrupt handler
	 * 
	 * @return The call-back function (interrupt handler)
	 */
	public VoidFunctionPtr getHandler() {
		return mHandler;
	}

	/**
	 * Gets the interrupt handler argument.
	 * 
	 * @return The interrupt handler argument
	 */
	public Object getArgument() {
		return mArg;
	}

	/**
	 * Gets the time when the interrupt should fire.
	 * 
	 * @return The time when the interrupt should fire.
	 */
	public int getWhen() {
		return mWhen;
	}

	/**
	 * Gets the type of interrupt
	 * 
	 * @return The type of interrupt
	 */
	public InterruptType getType() {
		return mType;
	}
}

/**
 * Data structures to emulate low-level interrupt hardware.
 *
 * The hardware provides a routine (SetLevel) to enable or disable interrupts.
 *
 * In order to emulate the hardware, we need to keep track of all interrupts the
 * hardware devices would cause, and when they are supposed to occur.
 *
 * This module also keeps track of simulated time. Time advances only when the
 * following occur: interrupts are re-enabled a user instruction is executed
 * there is nothing in the ready queue
 *
 * As a result, unlike real hardware, interrupts (and thus time-slice context
 * switches) cannot occur anywhere in the code where interrupts are enabled, but
 * rather only at those places in the code where simulated time advances (so
 * that it becomes time to invoke an interrupt in the hardware simulation).
 *
 * NOTE: this means that incorrectly synchronized code may work fine on this
 * hardware simulation (even with randomized time slices), but it wouldn't work
 * on real hardware. (Just because we can't always detect when your program
 * would fail in real life, does not mean it's ok to write incorrectly
 * synchronized code!)
 */
public class Interrupt {

	/**
	 * JNachos can be running kernel code (SystemMode), user code (UserMode), or
	 * there can be no runnable thread, because the ready list is empty
	 * (IdleMode).
	 */
	public final static int IdleMode = 0;
	public final static int SystemMode = 1;
	public final static int UserMode = 2;

	/** Strings for debug display. */
	public final static String intLevelNames[] = { "off", "on" };

	/** Strings for debug display of InterruptTypes */
	public final static String intTypeNames[] = { "timer", "disk", "console write", "console read", "network send",
			"network recv" };

	/** Are interrupts enabled or disabled? */
	private static boolean mEnabled;

	/** The list of interrupts scheduled to occur in the future. */
	private static LinkedList<PendingInterrupt> mPending;

	/** true if we are running an interrupt handler */
	private static boolean mInHandler;

	/**
	 * true if we are to context switch on return from the interrupt handler.
	 */
	private static boolean mYieldOnReturn;

	/** current status. */
	private static int mStatus; // idle, kernel mode, user mode

	/**
	 * Gets the current interrupt status.
	 * 
	 * @return The current interrupt status.
	 */
	public static int getStatus() {
		return mStatus;
	}

	/**
	 * Initialize the simulation of hardware device interrupts. Interrupts start
	 * disabled, with no interrupts pending, etc.
	 */
	public static void init() {
		mEnabled = false;
		mPending = new LinkedList<PendingInterrupt>();
		mInHandler = false;
		mYieldOnReturn = false;
		mStatus = SystemMode;
	}

	/**
	 * De-allocate the data structures needed by the interrupt simulation.
	 * Iterate through the list of pending interrupts and remove them from the
	 * lsit.
	 */
	public static void killInterrupt() {
		// while there are more interrupts
		while (!mPending.isEmpty()) {
			// remove the interrupt
			mPending.remove();
		}

		// set the list to null
		mPending = null;
	}

	/**
	 * Change interrupts to be enabled or disabled, without advancing the
	 * simulated time (normally, enabling interrupts advances the time).
	 *
	 * Used internally.
	 *
	 * @param old
	 *            the old interrupt mStatus.
	 * @param now
	 *            the new interrupt mStatus.
	 */
	public static void changeLevel(boolean pOld, boolean pNow) {
		mEnabled = pNow;
		Debug.print('i', "\tinterrupts:" + pOld + "-> " + pNow);
	}

	/**
	 * Change interrupts to be enabled or disabled, and if interrupts are being
	 * enabled, advance simulated time by calling OneTick().
	 *
	 * @return The old interrupt mStatus.
	 * @param now
	 *            the new interrupt mStatus.
	 */
	public static boolean setLevel(boolean pNow) {
		boolean old = mEnabled;

		// interrupt handlers are prohibited from enabling interrupts
		assert ((pNow == false) || (mInHandler == false));

		// change to new state
		changeLevel(old, pNow);

		// advance simulated time
		if ((pNow == true) && (old == false)) {
			oneTick();
		}

		// return the previous level
		return old;
	}

	/**
	 * Gets the current interrupt level.
	 * 
	 * @return The current interrupt level.
	 */
	public static boolean getLevel() {
		return mEnabled;
	}

	/**
	 * Sets the current interrupt level.
	 * 
	 * @param pStatus
	 *            the new interrupt level.
	 */
	public static void setStatus(int pStatus) {
		mStatus = pStatus;
	}

	/**
	 * Turn interrupts on. Who cares what they used to be? Used in ThreadRoot,
	 * to turn interrupts on when first starting up a thread.
	 */
	public static void enable() {
		setLevel(true);
	}

	/**
	 * Advance simulated time and check if there are any pending interrupts to
	 * be called.
	 *
	 * Two things can cause OneTick to be called: interrupts are re-enabled a
	 * user instruction is executed
	 */
	public static void oneTick() {
		// advance simulated time
		if (mStatus == SystemMode) {
			Statistics.totalTicks += Statistics.SystemTick;
			Statistics.systemTicks += Statistics.SystemTick;
		} else { // USER_PROGRAM
			Statistics.totalTicks += Statistics.UserTick;
			Statistics.userTicks += Statistics.UserTick;
		}

		Debug.print('i', "\n== Tick " + Statistics.totalTicks + "==");

		// check any pending interrupts are now ready to fire
		// first, turn off interrupts (interrupt handlers run with interrupts
		// disabled)
		changeLevel(true, false);

		// check for pending interrupts
		while (checkIfDue(false))
			;

		// re-enable interrupts
		changeLevel(false, true);

		// if the timer device handler asked
		// for a context switch, ok to do it now
		if (mYieldOnReturn) {
			mYieldOnReturn = false;
			JNachos.getCurrentProcess().yield();
		}
	}

	/**
	 * Called from within an interrupt handler, to cause a context switch (for
	 * example, on a time slice) in the interrupted thread, when the handler
	 * returns.
	 *
	 * We can't do the context switch here, because that would switch out the
	 * interrupt handler, and we want to switch out the interrupted thread.
	 */
	public static void yieldOnReturn() {
		assert (mInHandler);
		mYieldOnReturn = true;
	}

	/**
	 * Routine called when there is nothing in the ready queue.
	 *
	 * Since something has to be running in order to put a thread on the ready
	 * queue, the only thing to do is to advance simulated time until the next
	 * scheduled hardware interrupt.
	 *
	 * If there are no pending interrupts, stop. There's nothing more for us to
	 * do.
	 */
	public static void idle() {
		Debug.print('i', "Machine idling; checking for interrupts.\n");
		mStatus = IdleMode;

		// check for any pending interrupts
		if (checkIfDue(true)) {
			// check for any other pending interrupts
			while (checkIfDue(false))
				;

			// Since there's nothing in the
			// ready queue, the yield is automatic
			mYieldOnReturn = false;

			// we are in the kernel
			mStatus = SystemMode;

			// return in case there's now a runnable process
			return;
		}

		// if there are no pending interrupts, and nothing is on the ready
		// queue, it is time to stop. If the console or the network is
		// operating, there are *always* pending interrupts, so this code
		// is not reached. Instead, the halt must be invoked by the user
		// program.
		Debug.print('i', "Machine idle.  No interrupts to do.\n");
		System.out.println("No threads ready or runnable, and no pending interrupts.\n");
		System.out.println("Assuming the program completed.\n");
		halt();
	}

	/**
	 * Shut down Nachos cleanly, printing out performance statistics.
	 */
	public static void halt() {
		System.out.println("Machine halting!\n\n");
		Statistics.Print();
		JNachos.cleanUp(); // Never returns.
	}

	/**
	 * Arrange for the CPU to be interrupted when simulated time reaches "now +
	 * when".
	 *
	 * Implementation: just put it on a sorted list.
	 *
	 * NOTE: the Nachos kernel should not call this routine directly. Instead,
	 * it is only called by the hardware device simulators.
	 *
	 * @param handler
	 *            the procedure to call when the interrupt occurs
	 * @param arg
	 *            the argument to pass to the procedure
	 * @param fromNow
	 *            how far in the future (in simulated time) the interrupt is to
	 *            occur
	 * @param type
	 *            the hardware device that generated the interrupt
	 */
	public static void schedule(VoidFunctionPtr handler, Object arg, int fromNow, InterruptType type) {
		int when = Statistics.totalTicks + fromNow;
		PendingInterrupt toOccur = new PendingInterrupt(handler, arg, when, type);

		Debug.print('i', "Scheduling interrupt handler the " + intTypeNames[type.ordinal()] + " at time =" + when);

		assert (fromNow > 0);

		SortedInsert(toOccur, when);
	}

	/**
	 * Check if an interrupt is scheduled to occur, and if so, fire it off.
	 *
	 * @return true, if we fired off any interrupt handlers
	 * 
	 * @param advanceClock
	 *            if true, there is nothing in the ready queue, so we should
	 *            simply advance the clock to when the next pending interrupt
	 *            would occur (if any). If the pending interrupt is just the
	 *            time-slice daemon, however, then we're done!
	 */
	public static boolean checkIfDue(boolean advanceClock) {
		// MachineStatus
		int old = mStatus;
		int when;

		// interrupts need to be disabled, to invoke an interrupt handler
		assert (mEnabled == false);

		if (Debug.isEnabled('i'))
			dumpState();

		if (mPending.isEmpty())
			return false;

		PendingInterrupt toOccur = mPending.removeFirst();

		if (advanceClock && toOccur.getWhen() > Statistics.totalTicks) { // advance
																			// the
																			// clock
			Statistics.idleTicks += (toOccur.getWhen() - Statistics.totalTicks);
			Statistics.totalTicks = toOccur.getWhen();
		} else if (toOccur.getWhen() > Statistics.totalTicks) { // not time yet,
																// put it back
			mPending.addFirst(toOccur);
			return false;
		}

		// Check if there is nothing more to do, and if so, quit
		if ((mStatus == IdleMode) && (toOccur.getType() == InterruptType.TimerInt) && mPending.isEmpty()) {
			SortedInsert(toOccur, toOccur.getWhen());
			return false;
		}

		Debug.print('i', "Invoking interrupt handler for the " + intTypeNames[toOccur.getType().ordinal()] + " at time "
				+ toOccur.getWhen());

		Machine.delayedLoad(0, 0);
		mInHandler = true;
		mStatus = SystemMode; // whatever we were doing,
		// we are now going to be
		// running in the kernel

		toOccur.getHandler().call(toOccur.getArgument()); // call the interrupt
															// handler
		mStatus = old; // restore the machine mStatus
		mInHandler = false;
		toOccur = null;
		return true;
	}

	/**
	 * Print information about an interrupt that is scheduled to occur. When,
	 * where, why, etc.
	 */
	public static void PrintPending(PendingInterrupt arg) {
		PendingInterrupt pend = arg;
		System.out.println(
				"Interrupt handler " + intTypeNames[pend.getType().ordinal()] + ", scheduled at " + pend.getWhen());
	}

	/**
	 * Print the complete interrupt state - the mStatus, and all interrupts that
	 * are scheduled to occur in the future.
	 */
	public static void dumpState() {
		System.out.println("Time: " + Statistics.totalTicks + ", interrupts " + mEnabled);
		System.out.println("Pending interrupts:\n");
		System.out.flush();
		// pending.Mapcar(PrintPending);
		System.out.println("End of pending interrupts\n");
		System.out.flush();
	}

	/**
	 * Inserts the pending interrupt into the list of pending interrupts in a
	 * sorted manner.
	 * 
	 * @param pInterrupt
	 *            The interrupt to add to the list of pending interrupts
	 * @param pWhen
	 *            The time when the interrupt should be thrown.
	 */
	public static void SortedInsert(PendingInterrupt pPending, int pWhen) {
		for (int i = 0; i < mPending.size(); i++) {
			PendingInterrupt next = mPending.get(i);
			if (next.getWhen() > pWhen) {
				mPending.add(i, pPending);
				return;
			}
		}
		mPending.addLast(pPending);
	}
}
