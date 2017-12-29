/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

/**
 * This interface defines the api that a class must support in order to be
 * considered a filesystem
 * 
 *
 */
public interface FileSystem {

	/**
	 * Creates the specified file.
	 * 
	 * @param pFileName
	 *            The file to create
	 * @param pInitialSize
	 *            the initial size of the file
	 * @return true if successful, false otherwise
	 */
	public boolean create(String pFileName, int pInitialSize);

	/**
	 * 
	 * @param pFileName
	 *            the name of the file to open.
	 * @return The OpenFile or null if the file does not exit
	 */
	public OpenFile open(String pFileName);

	/**
	 * 
	 * @param pFileName
	 *            the name of the file to remove.
	 * @return true if successful, false otherwise
	 */
	public boolean remove(String pFileName);

	/**
	 * Displays the list of files stored.
	 */
	public void list();

	/**
	 * Displays the files in a meaningful way.
	 */
	public void print();

}
