/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

import jnachos.kern.*;

/**
 * This class simulates a harddrive disk and the disk geometry.
 */
public class Disk implements VoidFunctionPtr {
	/** Number of bytes per disk sector. */
	public static final int SectorSize = 128;

	/** Number of sectors per disk track. */
	public static final int SectorsPerTrack = 32;

	/** Number of tracks per disk. */
	public static final int NumTracks = 32;

	/** Total # of sectors per disk. */
	public static final int NumSectors = (SectorsPerTrack * NumTracks);

	/** Nachos Magic Number. */
	public static final int MagicNumber = 0x456789ab;

	/** The size of a magic number. */
	public static final int MagicSize = 4;

	/** Calculate the size of the disk. */
	public static final int DiskSize = (MagicSize + (NumSectors * SectorSize));

	/** UNIX file number for simulated disk. */
	private int mFileno;

	/**
	 * Interrupt handler, to be invoked when any disk request finishes.
	 */
	private VoidFunctionPtr mHandler;

	/** Argument to interrupt handler. */
	private Object mHandlerArg;

	/** Is a disk operation in progress? */
	private boolean mActive;

	/** The previous disk request. */
	private int mLastSector;

	/**
	 * When the track buffer started being loaded.
	 */
	private int mBufferInit;

	/**
	 * Initialize a simulated disk. Open the file (creating it if it doesn't
	 * exist), and check the magic number to make sure it's OK to treat it as
	 * JNachos disk storage.
	 *
	 * @param name
	 *            the text name of the file simulating the Nachos disk.
	 * @param callWhenDone
	 *            the interrupt handler to be called when disk read/write
	 *            request completes.
	 * @param callArg
	 *            the argument to pass the interrupt handler.
	 */
	public Disk(String name, VoidFunctionPtr callWhenDone, Object callArg) {
		int magicNum;
		int tmp = 0;

		Debug.print('d', "Initializing the disk, " + callWhenDone + ", " + callArg);

		mHandler = callWhenDone;
		mHandlerArg = callArg;
		mLastSector = 0;
		mBufferInit = 0;

		mFileno = JavaSys.openForReadWrite(name, false);

		// file exists, check magic number
		if (mFileno >= 0) {
			byte buffer[] = new byte[MagicSize];
			JavaSys.lseek(mFileno, 0);
			JavaSys.read(mFileno, buffer, MagicSize);

			for (int i = 0; i < 4; i++) {
				Debug.print('d', i + "::" + buffer[i]);
			}

			magicNum = JavaSys.bytesToInt(buffer, 0).intValue();
			Debug.print('j', "Magic: " + magicNum);

			assert (magicNum == MagicNumber);
		}

		// file doesn't exist, create it
		else {
			mFileno = JavaSys.openForWrite(name);
			byte[] magicArray = new byte[4];

			JavaSys.intToBytes(MagicNumber, magicArray, 0);

			for (int i = 0; i < 4; i++) {
				Debug.print('j', i + "::" + magicArray[i]);
			}

			Debug.print('j', "Translated:" + JavaSys.bytesToInt(magicArray, 0));
			// write magic number
			JavaSys.lseek(mFileno, 0);
			JavaSys.writeFile(mFileno, magicArray, MagicSize);

			Debug.print('j', "DS:" + DiskSize);

			// need to write at end of file, so that reads will not return EOF
			JavaSys.lseek(mFileno, DiskSize - 4);
			byte[] buf = new byte[4];
			JavaSys.intToBytes(0, buf, 0);

			JavaSys.writeFile(mFileno, buf, 4);
		}

		mActive = false;
	}

	/**
	 * Called when it is time to invoke the disk interrupt handler, to tell the
	 * Nachos kernel that the disk request is done.
	 * 
	 * @param pDummy
	 *            not used.
	 */
	public void call(Object pDummy) {
		mActive = false;
		mHandler.call(mHandlerArg);
	}

	/**
	 * Clean up disk simulation, by closing the UNIX file representing the disk.
	 */
	public void delete() {
		JavaSys.close(mFileno);
	}

	/**
	 * Dump the data in a disk read/write request, for Debug.printing.
	 * 
	 * @param data
	 *            the sector data to print.
	 * @param writing
	 *            indicates if the sector was to be written or read.
	 * @param sector
	 *            the sector number that this data is from.
	 */
	static void printSector(boolean writing, int sector, byte[] data) {
		if (writing) {
			System.out.println("Writing sector: " + sector);
		} else {
			System.out.println("Reading sector: " + sector);
		}

		for (int i = 0; i < (SectorSize / 4); i++) {
			System.out.println(i + " : " + data[i]);
		}
	}

	/**
	 * Simulate a request to read a single disk sector Do the read immediately
	 * to the file Set up an interrupt handler to be called later, that will
	 * notify the caller when the simulator says the operation has completed.
	 *
	 * Note that a disk only allows an entire sector to be read, not part of a
	 * sector.
	 *
	 * @param sectorNumber
	 *            the disk sector to read.
	 * @param the
	 *            buffer to hold the incoming bytes.
	 */
	public void readRequest(int sectorNumber, byte[] data) {
		int ticks = computeLatency(sectorNumber, false);

		assert (!mActive);
		// only one request at a time
		assert ((sectorNumber >= 0) && (sectorNumber < NumSectors));

		Debug.print('d', "Reading from sector" + sectorNumber);

		JavaSys.lseek(mFileno, SectorSize * sectorNumber + MagicSize);
		JavaSys.read(mFileno, data, SectorSize);

		if (Debug.isEnabled('d')) {
			printSector(false, sectorNumber, data);
		}

		mActive = true;
		updateLast(sectorNumber);

		Statistics.numDiskReads++;

		Interrupt.schedule(this, null, ticks, InterruptType.DiskInt);
	}

	/**
	 * Simulate a request to write a single disk sector Do the write immediately
	 * to the file Set up an interrupt handler to be called later, that will
	 * notify the caller when the simulator says the operation has completed.
	 *
	 * Note that a disk only allows an entire sector to be written, not part of
	 * a sector.
	 *
	 * @param sectorNumber
	 *            the disk sector to write.
	 * @param data
	 *            the bytes to be written.
	 */
	public void writeRequest(int sectorNumber, byte[] data) {
		int ticks = computeLatency(sectorNumber, true);

		assert (!mActive);
		assert ((sectorNumber >= 0) && (sectorNumber < NumSectors));

		Debug.print('d', "Writing to sector " + sectorNumber);
		Debug.print('d', "Location: " + SectorSize * sectorNumber + MagicSize);
		JavaSys.lseek(mFileno, SectorSize * sectorNumber + MagicSize);
		JavaSys.writeFile(mFileno, data, SectorSize);

		if (Debug.isEnabled('d')) {
			printSector(true, sectorNumber, data);
		}

		mActive = true;
		updateLast(sectorNumber);

		Statistics.numDiskWrites++;
		Interrupt.schedule(this, null, ticks, InterruptType.DiskInt);
	}

	/**
	 * Returns how long it will take to position the disk head over the correct
	 * track on the disk. Since when we finish seeking, we are likely to be in
	 * the middle of a sector that is rotating past the head, we also return how
	 * long until the head is at the next sector boundary.
	 * 
	 * Disk seeks at one track per SeekTime ticks (cf. stats.h) and rotates at
	 * one sector per RotationTime ticks
	 */
	public int timeToSeek(int newSector, int[] rotation) {
		int newTrack = newSector / SectorsPerTrack;
		int oldTrack = mLastSector / SectorsPerTrack;
		int seek = Math.abs(newTrack - oldTrack) * Statistics.SeekTime;
		// how long will seek take?
		int over = (Statistics.totalTicks + seek) % Statistics.RotationTime;
		// will we be in the middle of a sector when
		// we finish the seek?

		rotation[0] = 0;
		if (over > 0) // if so, need to round up to next full sector
		{
			rotation[0] = Statistics.RotationTime - over;
		}
		return seek;
	}

	/**
	 * Return number of sectors of rotational delay between target sector "to"
	 * and current sector position "from"
	 */
	public int moduloDiff(int to, int from) {
		int toOffset = to % SectorsPerTrack;
		int fromOffset = from % SectorsPerTrack;

		return ((toOffset - fromOffset) + SectorsPerTrack) % SectorsPerTrack;
	}

	/**
	 * Return how long will it take to read/write a disk sector, from the
	 * current position of the disk head.
	 *
	 * Latency = seek time + rotational latency + transfer time Disk seeks at
	 * one track per SeekTime ticks (cf. stats.h) and rotates at one sector per
	 * RotationTime ticks
	 *
	 * To find the rotational latency, we first must figure out where the disk
	 * head will be after the seek (if any). We then figure out how long it will
	 * take to rotate completely past newSector after that point.
	 *
	 * The disk also has a "track buffer"; the disk continuously reads the
	 * contents of the current disk track into the buffer. This allows read
	 * requests to the current track to be satisfied more quickly. The contents
	 * of the track buffer are discarded after every seek to a new track.
	 */
	public int computeLatency(int newSector, boolean writing) {
		int[] rotation = new int[1];
		int seek = timeToSeek(newSector, rotation);
		int timeAfter = Statistics.totalTicks + seek + rotation[0];

		/*
		 * // turn this on if you don't want the track buffer stuff // check if
		 * track buffer applies if ((writing == false) && (seek == 0) &&
		 * (((timeAfter - bufferInit) / RotationTime) > ModuloDiff(newSector,
		 * bufferInit / RotationTime))) { Debug.print('d',
		 * "Request latency = %d\n", RotationTime); return RotationTime; // time
		 * to transfer sector from the track buffer }
		 */

		rotation[0] += moduloDiff(newSector, timeAfter / Statistics.RotationTime) * Statistics.RotationTime;

		Debug.print('d', "Request latency = " + seek + rotation + Statistics.RotationTime);

		return (seek + rotation[0] + Statistics.RotationTime);
	}

	/**
	 * Keep track of the most recently requested sector. So we can know what is
	 * in the track buffer.
	 */
	public void updateLast(int newSector) {
		int[] rotate = new int[1];
		int seek = timeToSeek(newSector, rotate);

		if (seek != 0) {
			mBufferInit = Statistics.totalTicks + seek + rotate[0];
		}

		mLastSector = newSector;

		Debug.print('d', "Updating last sector = " + mLastSector + " ," + mBufferInit);
	}
}
