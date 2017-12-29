/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.userbin;

/**
 * Data structures defining the Nachos Object Code Format (NOFF).
 *
 * Basically, we only know about three types of segments: code (read-only),
 * initialized data, and unitialized data
 */
public class NoffHeader {
	/** Magic number denoting Nachos object code file. */
	public static int NOFFMAGIC = 0xbadfad;

	/** The size of the header. */
	public static int size = 40;

	/** Should be NOFFMAGIC */
	public int noffMagic;

	/** Executable code segment. */
	public Segment code;

	/** Initialized data segment. */
	public Segment initData;

	/** Uninitialized data segment should be zero'ed before use. */
	public Segment uninitData;

	/**
	 * Constructor for a noff header. Takes a byte array which describes the
	 * data in the header.
	 * 
	 * @param b
	 *            the buffer of bytes from a file that contains the header.
	 */
	public NoffHeader(byte[] b) {
		// Assert the buffer has the right length
		assert (b.length == size);

		// Get the magic number
		noffMagic = (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);

		// Read in the code segment
		code = new Segment(b, 4);

		// Read in the data segmant
		initData = new Segment(b, 16);

		// Read in the unit segment
		uninitData = new Segment(b, 28);
	}
}
