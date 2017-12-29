/** 
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/7/08.
 */
package jnachos.userbin;

/** Describes a segment of the Noff header. */
public class Segment {
	/** Location of segment in virt addr space. */
	public int virtualAddr;

	/** location of segment in this file. */
	public int inFileAddr;

	/** Size of segment. */
	public int size;

	/**
	 * Segment Constructor.
	 * 
	 * @param b
	 *            The buffer of bytes which holds the segment
	 * @param start
	 *            the starting location in the buffer of the data
	 **/
	public Segment(byte[] b, int start) {
		// Read in the virtual address
		virtualAddr = (b[start] << 24) + ((b[start + 1] & 0xFF) << 16) + ((b[start + 2] & 0xFF) << 8)
				+ (b[start + 3] & 0xFF);

		// Read in the in file address
		inFileAddr = (b[start + 4] << 24) + ((b[start + 5] & 0xFF) << 16) + ((b[start + 6] & 0xFF) << 8)
				+ (b[start + 7] & 0xFF);

		// Read in the number of bytes in this segment
		size = (b[start + 8] << 24) + ((b[start + 9] & 0xFF) << 16) + ((b[start + 10] & 0xFF) << 8)
				+ (b[start + 11] & 0xFF);
	}
}
