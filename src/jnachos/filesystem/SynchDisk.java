/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

import jnachos.kern.sync.*;
import jnachos.machine.*;
import jnachos.kern.VoidFunctionPtr;

/**
 * 
 * @author pjmcswee
 *
 */
public class SynchDisk implements VoidFunctionPtr {
	/** Raw disk device. */
	Disk mDisk;

	/**
	 * To synchronize requesting thread with the interrupt handler.
	 */
	Semaphore mSemaphore;

	/**
	 * Only one read/write request can be sent to the disk at a time.
	 */
	Lock mLock;

	/**
	 * Disk interrupt handler. Need this to be a C routine, because C++ can't
	 * handle pointers to member functions.
	 */
	public void call(Object dummy) {
		requestDone();
	}

	/**
	 * Initialize the synchronous interface to the physical disk, in turn
	 * initializing the physical disk.
	 *
	 * @param pName
	 *            UNIX file name to be used as storage for the disk data
	 *            (usually, "DISK")
	 */
	public SynchDisk(String pName) {
		mSemaphore = new Semaphore("synch disk", 0);
		mLock = new Lock("synch disk lock");
		mDisk = new Disk(pName, (VoidFunctionPtr) this, null);
	}

	/**
	 * De-allocate data structures needed for the synchronous disk abstraction.
	 */
	public void delete() {
		mDisk.delete();
		mLock.delete();
		mSemaphore.delete();
	}

	/**
	 * Read the contents of a disk sector into a buffer. Return only after the
	 * data has been read.
	 *
	 * @param sectorNumber
	 *            the disk sector to read.
	 * @param data
	 *            the buffer to hold the contents of the disk sector.
	 */

	public void readSector(int sectorNumber, byte[] data) {
		mLock.acquire(); // only one disk I/O at a time
		mDisk.readRequest(sectorNumber, data);
		mSemaphore.P(); // wait for interrupt
		mLock.release();
	}

	/**
	 * Write the contents of a buffer into a disk sector. Return only after the
	 * data has been written.
	 *
	 * @param sectorNumber
	 *            the disk sector to be written
	 * @param data
	 *            the new contents of the disk sector
	 */

	public void writeSector(int sectorNumber, byte[] data) {
		mLock.acquire(); // only one disk I/O at a time
		mDisk.writeRequest(sectorNumber, data);
		mSemaphore.P(); // wait for interrupt
		mLock.release();
	}

	/**
	 * Disk interrupt handler. Wake up any thread waiting for the disk request
	 * to finish.
	 */

	public void requestDone() {
		mSemaphore.V();
	}

}
