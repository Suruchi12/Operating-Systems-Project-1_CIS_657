/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

import jnachos.machine.*;
import jnachos.kern.*;

/**
 * The following class defines the JNachos "file header" (in UNIX terms, the
 * "i-node"), describing where on disk to find all of the data in the file. The
 * file header is organized as a simple table of pointers to data blocks.
 * 
 * The file header data structure can be stored in memory or on disk. When it is
 * on disk, it is stored in a single sector -- this means that we assume the
 * size of this data structure to be the same as one disk sector. Without
 * indirect addressing, this limits the maximum file length to just under 4K
 * bytes.
 * 
 * There is no constructor; rather the file header can be initialized by
 * allocating blocks for the file (if it is a new file), or by reading it from
 * disk.
 */
public class FileHeader {
	/** The number of direct pointers */
	public static final int NumDirect = ((Disk.SectorSize - 2 * 4) / 4);

	/** The maximum size of a file in JNachos */
	public static final int MaxFileSize = (NumDirect * Disk.SectorSize);

	/** Number of bytes in the file. */
	private int mNumBytes;

	/** Number of data sectors in the file. */
	private int mNumSectors;

	/** Disk sector numbers for each data block in the file. */
	private int[] mDataSectors;

	/**
	 * Default constructor.
	 */
	public FileHeader() {
		mDataSectors = new int[NumDirect];
	}

	/**
	 * 
	 */
	public void delete() {

	}

	/**
	 * Initialize a fresh file header for a newly created file. Allocate data
	 * blocks for the file out of the map of free disk blocks.
	 * 
	 *
	 * @param pFreeMap
	 *            the bit map of free disk sectors.
	 * @param pFileSize
	 *            the bit map of free disk sectors.
	 * @return true if successful, false if there are not enough free blocks to
	 *         accomodate the new file.
	 */
	public boolean allocate(BitMap pFreeMap, int pFileSize) {
		mNumBytes = pFileSize;
		mNumSectors = (int) Math.ceil((double) pFileSize / (double) Disk.SectorSize);

		// not enough space
		if (pFreeMap.numClear() < mNumSectors) {
			return false;
		}

		// Find enough sectors
		for (int i = 0; i < mNumSectors; i++) {
			mDataSectors[i] = pFreeMap.find();
		}
		return true;
	}

	/**
	 * De-allocate all the space allocated for data blocks for this file.
	 *
	 * @param pFreeMap
	 *            the bit map of free disk sectors.
	 * @throws Assertion
	 *             Error if the
	 */

	public void deallocate(BitMap pFreeMap) {
		for (int i = 0; i < mNumSectors; i++) {
			// ought to be marked!
			assert (pFreeMap.test(mDataSectors[i]));
			pFreeMap.clear(mDataSectors[i]);
		}
	}

	/**
	 * Fetch contents of file header from disk.
	 *
	 * @param sector
	 *            the disk sector containing the file header
	 */
	public void fetchFrom(int sector) {
		byte[] buffer = new byte[Disk.SectorSize];
		JNachos.mSynchDisk.readSector(sector, buffer);

		mNumBytes = JavaSys.bytesToInt(buffer, 0);
		mNumSectors = JavaSys.bytesToInt(buffer, 4);

		for (int i = 0; i < mNumSectors; i++) {
			mDataSectors[i] = JavaSys.bytesToInt(buffer, 8 + i * 4);
		}

	}

	/**
	 * Write the modified contents of the file header back to disk.
	 *
	 * @param sector
	 *            the disk sector to contain the file header.
	 */
	public void writeBack(int sector) {

		byte[] buffer = new byte[Disk.SectorSize];

		JavaSys.intToBytes(mNumBytes, buffer, 0);
		JavaSys.intToBytes(mNumSectors, buffer, 4);

		for (int i = 0; i < mNumSectors; i++) {
			JavaSys.intToBytes(mDataSectors[i], buffer, 8 + i * 4);
		}

		JNachos.mSynchDisk.writeSector(sector, buffer);
	}

	/**
	 * Converts virtual address to sector number. This is essentially a
	 * translation from a virtual address (the offset in the file) to a physical
	 * address (the sector where the data at the offset is stored).
	 *
	 * @param offset
	 *            the location within the file of the byte in question
	 * @return The disk sector which is storing a particular byte within the
	 *         file.
	 */
	public int byteToSector(int offset) {
		return (mDataSectors[offset / Disk.SectorSize]);
	}

	/**
	 * Return the number of bytes in the file.
	 */
	public int fileLength() {
		return mNumBytes;
	}

	/**
	 * Print the contents of the file header, and the contents of all the data
	 * blocks pointed to by the file header.
	 */
	public void print() {
		int i, j, k;
		byte[] data = new byte[Disk.SectorSize];

		System.out.println("FileHeader contents.  File size: " + mNumBytes + " File blocks:");

		for (i = 0; i < mNumSectors; i++) {
			System.out.println(mDataSectors[i] + " ");
		}

		System.out.println("File contents:");

		for (i = k = 0; i < mNumSectors; i++) {
			JNachos.mSynchDisk.readSector(mDataSectors[i], data);
			// for (j = 0; (j < Disk.SectorSize) && (k < mNumBytes); j++, k++)
			// {
			System.out.println(new String(data));
			/*
			 * if ('\040' <= data[j] && data[j] <= '\176') // isprint(data[j])
			 * System.out.println(new String(data[j])); else
			 * System.out.println(new String(data[j]));
			 */
			// }
		}
		// delete data;
	}

}
