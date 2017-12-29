/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

/**
 * This class keeps track of an array of bits. Primarily for keeping track of
 * free sectors/pageframes, etc. The main methods involved are find and clear.
 * Find will return an arbitrary bit (AFTER it has set the bit to true), clear
 * will clear out a specified bit.
 * 
 * The bitmap is written to and read from a file for storing.
 * 
 *
 */
public class BitMap {
	/** The number of clear bits. */
	private int mNumClear;

	/** Keep track of all of the bits. */
	private boolean[] mUsed;

	/** Size of the bit array. */
	private int mNumBits;

	/**
	 * Creates a bitmap witht the specified size.
	 * 
	 * @param pBits
	 *            the size of this bitmap.
	 */
	public BitMap(int pBits) {
		mUsed = new boolean[pBits];
		mNumClear = mUsed.length;
		mNumBits = pBits;
	}

	/**
	 * Returns the number of unused bits.
	 * 
	 * @return the number of unused bits.
	 */
	public int numClear() {
		return mNumClear;
	}

	/**
	 * Set the bit pBit to false (unused).
	 * 
	 * @param pBit
	 *            the bit to mark as unused.
	 * @throws Assertion
	 *             Error if the pBit is less than 0 or greater than the number
	 *             of bits.
	 */
	public void clear(int pBit) {
		assert ((pBit >= 0) && (pBit < mNumBits));

		if (mUsed[pBit]) {
			mNumClear++;
		}

		mUsed[pBit] = false;
	}

	/**
	 * Prints the bitmap to the screen.
	 */
	public void print() {
		for (int i = 0; i < mNumBits; i++) {
			System.out.println(i + ":" + mUsed[i] + ", ");
		}
	}

	/**
	 * Saves the bitmap to a file.
	 * 
	 * @param pFile
	 *            The file to write the bitmap to.
	 */
	public void writeBack(NachosOpenFile pFile) {
		byte[] buffer = new byte[mNumBits];
		for (int i = 0; i < mNumBits; i++) {
			buffer[i] = (byte) (mUsed[i] ? 1 : 0);
		}

		pFile.writeAt(buffer, mNumBits, 0);
	}

	/**
	 * Loads the bitmap from the file.
	 * 
	 * @param pFile
	 *            The file to Load the bitmap from.
	 */
	public void fetchFrom(NachosOpenFile pFile) {
		byte[] buffer = new byte[mNumBits];
		pFile.readAt(buffer, mNumBits, 0);

		for (int i = 0; i < mNumBits; i++) {
			mUsed[i] = (buffer[i] == 1);
		}
	}

	/**
	 * Checks whether or not the specified bit is in use.
	 * 
	 * @param pBit
	 *            the bit to check if used or not.
	 * @return True if the bit is used, false otherwise.
	 * @throws Assertion
	 *             Error if the pBit is less than 0 or greater than the number
	 *             of bits.
	 */
	public boolean test(int pBit) {
		assert ((pBit >= 0) && (pBit < mNumBits));

		return mUsed[pBit];
	}

	/**
	 * Marks the specified bit as used.
	 * 
	 * @param pBit
	 *            The bit to mark as used.
	 * @throws Assertion
	 *             Error if the pBit is less than 0 or greater than the number
	 *             of bits.
	 *
	 */
	public void mark(int pBit) {
		assert ((pBit >= 0) && (pBit < mNumBits));

		if (!mUsed[pBit])
			mNumClear--;

		mUsed[pBit] = true;

	}

	/**
	 *
	 */
	public void delete() {
		// ??
	}

	/**
	 * Function is used to find and mark an unused bit.
	 * 
	 * @return The first index of an unused bit if there is one, -1 otherwise.
	 */
	public int find() {
		if (mNumClear == 0)
			return -1;
		for (int i = 0; i < mNumBits; i++) {
			if (!mUsed[i]) {
				mark(i);
				return i;
			}
		}
		return -1;
	}

}
