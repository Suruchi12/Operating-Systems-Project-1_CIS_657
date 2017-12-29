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
 * The JavaFileSystem just piggy-backs the file system presented to JNachos from
 * the actual native filesystem through the Java API. Any file
 * 
 * @author pjmcswee
 *
 */
public class JavaFileSystem implements FileSystem {

	/**
	 * What to do here?....
	 * 
	 * @param pFormat
	 *            whether or not
	 */
	public JavaFileSystem(boolean pFormat) {
	}

	/**
	 * Creates the file specified.
	 * 
	 * @param pName
	 *            the name of the file.
	 * @param initialSize
	 *            the initial size of the file.
	 */
	public boolean create(String pName, int initialSize) {
		int fileDescriptor = JavaSys.openForWrite(pName);

		if (fileDescriptor == -1) {
			return false;
		}

		JavaSys.close(fileDescriptor);
		return true;
	}

	/**
	 * 
	 * @param pName
	 *            the name of the file to open.
	 * @return The openfile or null if the file does not exist.
	 */
	public OpenFile open(String pName) {
		int fileDescriptor = JavaSys.openForReadWrite(pName, false);

		System.out.println("here");
		if (fileDescriptor == -1) {
			return null;
		}

		return new JavaOpenFile(fileDescriptor);
	}

	/**
	 * Removes the file from JNachos.
	 * 
	 * @param pName
	 *            the name of the file to remove.
	 * @return true if the file is removed, false if the file does not exist
	 */
	public boolean remove(String pName) {
		// return JavaSys.unlink(pName) == 0;
		return true;
	}

	/**
	 * Displays all of the files.
	 */
	public void print() {
	}

	/**
	 * Lists all of the files in the filesystem
	 */
	public void list() {
	}
}
