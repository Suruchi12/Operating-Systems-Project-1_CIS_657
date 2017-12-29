/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

import jnachos.machine.*;

/**
 * 
 * @author pjmcswee
 *
 */
public class JavaOpenFile implements OpenFile {
	/** The file descriptor associated with the file. */
	private int mFile;

	/** The current position within the file. */
	private int currentOffset;

	/**
	 * 
	 * @param fd
	 *            the file descriptor for this file.
	 */
	public JavaOpenFile(int fd) {
		mFile = fd;
		currentOffset = 0;
	}

	/**
	 * Closes the file.
	 */
	public void closeFile() {
		JavaSys.close(mFile);
	}

	/**
	 * Reads a specified number of bytes.
	 * 
	 * @param position
	 *            the location in the file to read from.
	 * @param numBytes
	 *            the number of bytes to read.
	 * @param into
	 *            the buffer to read into.
	 * @return the number of bytes actually read.
	 */
	public int readAt(byte[] into, int numBytes, int position) {
		JavaSys.lseek(mFile, position);
		return JavaSys.readPartial(mFile, into, numBytes);
	}

	/**
	 * Writes a specified number of bytes into a file.
	 * 
	 * @param position
	 *            the location in the file to read from.
	 * @param numBytes
	 *            the number of bytes to read.
	 * @param from
	 *            the buffer to read from.
	 * @return the number of bytes actually read.
	 */
	public int writeAt(byte[] from, int numBytes, int position) {
		JavaSys.lseek(mFile, position);
		JavaSys.writeFile(mFile, from, numBytes);
		return numBytes;
	}

	/**
	 * Reads a specified number of bytes.
	 * 
	 * @param numBytes
	 *            the number of bytes to read.
	 * @param into
	 *            the buffer to read into.
	 * @return the number of bytes actually read.
	 */
	public int read(byte[] into, int numBytes) {
		int numRead = readAt(into, numBytes, currentOffset);
		currentOffset += numRead;
		return numRead;
	}

	/**
	 * Writes a specified number of bytes.
	 * 
	 * @param numBytes
	 *            the number of bytes to read.
	 * @param from
	 *            the buffer to read from.
	 * @return the number of bytes actually read.
	 */
	public int write(byte[] from, int numBytes) {
		int numWritten = writeAt(from, numBytes, currentOffset);
		currentOffset += numWritten;
		return numWritten;
	}

	/**
	 * Gets the length of the file.
	 * 
	 * @return the length of the file.
	 */
	public int length() {
		JavaSys.lseek(mFile, 0);
		return (int) JavaSys.tell(mFile);
	}
}
