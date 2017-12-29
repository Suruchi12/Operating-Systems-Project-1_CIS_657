/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

/**
 * This interface displays the api a class needs to support in order to be
 * considered an openfile.
 * 
 * @author pjmcswee
 *
 */
public interface OpenFile {
	/**
	 * 
	 */
	public void closeFile();

	/**
	 * 
	 * @param into
	 * @param numBytes
	 * @param position
	 * @return
	 */
	public int readAt(byte[] into, int numBytes, int position);

	/**
	 * 
	 * @param from
	 * @param numBytes
	 * @param position
	 * @return
	 */
	public int writeAt(byte[] from, int numBytes, int position);

	/**
	 * 
	 * @param into
	 * @param numBytes
	 * @return
	 */
	public int read(byte[] into, int numBytes);

	/**
	 * 
	 * @param from
	 * @param numBytes
	 * @return
	 */
	public int write(byte[] from, int numBytes);

	/**
	 * 
	 * @return
	 */
	public int length();
}
