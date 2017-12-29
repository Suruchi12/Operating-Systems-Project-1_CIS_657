/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.filesystem;

import jnachos.machine.*;
import jnachos.kern.Debug;

/**
 * Data structures to manage a UNIX-like directory of file names. A directory is
 * a table of pairs: <file name, sector #>, giving the name of each file in the
 * directory, and where to find its file header (the data structure describing
 * where to find the file's data blocks) on disk.
 * 
 * The directory is a table of fixed length entries; each entry represents a
 * single file, and contains the file name, and the location of the file header
 * on disk. The fixed size of each directory entry means that we have the
 * restriction of a fixed maximum size for file names.
 * 
 * The constructor initializes an empty directory of a certain size; we use
 * ReadFrom/WriteBack to fetch the contents of the directory from disk, and to
 * write back any modifications back to disk.
 * 
 * Also, this implementation has the restriction that the size of the directory
 * cannot expand. In other words, once all the entries in the directory are
 * used, no more files can be created. Fixing this is one of the parts to the
 * assignment.
 * 
 *
 */
public class Directory {

	/**
	 * for simplicity, we assume file names are <= 9 characters long
	 */
	public static final int FILENAMEMAXLEN = 9;

	/**
	 * The following class defines a "directory entry", representing a file in
	 * the directory. Each entry gives the name of the file, and where the
	 * file's header is to be found on disk.
	 *
	 * Internal data structures kept public so that Directory operations can
	 * access them directly.
	 */
	class DirectoryEntry {
		/** Is this directory entry in use? */
		public boolean mInUse;

		/**
		 * Location on disk to find the FileHeader for this file.
		 */
		public int mSector;

		/**
		 * Text name for file, with +1 for the trailing '\0'
		 */
		public char[] mName;

		/** DirectoryEntry constructor. */
		DirectoryEntry() {
		}

	}

	/**
	 * Computes how large a directory entry is.
	 * 
	 * @return The size of a directory entry
	 */
	public static int sizeOfDirectoryEntry() {
		return FILENAMEMAXLEN * 2 + 1 + 4;
	}

	/** Number of directory entries. */
	private int mTableSize;

	/**
	 * Table of pairs: <file name, file header location>
	 */
	private DirectoryEntry[] mTable;

	/**
	 * Initialize a directory; initially, the directory is completely empty. If
	 * the disk is being formatted, an empty directory is all we need, but
	 * otherwise, we need to call FetchFrom in order to initialize it from disk.
	 *
	 * @param pSize
	 *            the number of entries in the directory.
	 */

	public Directory(int pSize) {
		// Create teh table array
		mTable = new DirectoryEntry[pSize];

		// Save the table size
		mTableSize = pSize;

		// Create the directory taable
		for (int i = 0; i < mTableSize; i++) {
			mTable[i] = new DirectoryEntry();
			mTable[i].mInUse = false;
		}
	}

	/**
	 * De-allocate directory data structure.
	 */
	public void delete() {
		// delete table;
	}

	/**
	 * Read the contents of the directory from disk.
	 *
	 * @param file
	 *            the file containing the directory contents
	 */
	public void fetchFrom(OpenFile pFile) {

		int size = NachosFileSystem.NumDirEntries * sizeOfDirectoryEntry();
		byte[] buffer = new byte[size];
		pFile.readAt(buffer, size, 0);

		mTableSize = JavaSys.bytesToInt(buffer, 0).intValue();
		// mTable = new DirectoryEntry[mTableSize];
		for (int i = 0; i < mTableSize; i++) {

			mTable[i] = new DirectoryEntry();
			mTable[i].mInUse = (buffer[i * sizeOfDirectoryEntry() + 4] == (byte) 1);
			if (mTable[i].mInUse) {
				mTable[i].mSector = JavaSys.bytesToInt(buffer, i * sizeOfDirectoryEntry() + 5).intValue();
				byte[] bName = new byte[FILENAMEMAXLEN * 2];
				System.arraycopy(buffer, i * sizeOfDirectoryEntry() + 9, bName, 0, bName.length);
				mTable[i].mName = new String(bName).trim().toCharArray();
				// System.out.println("FILE: " + mTable[i].mName.t);
			}
			/*
			 * String sName = new String(mTable[i].mName); byte bName =
			 * sName.getBytes(); byte nameBuffer = new byte[FILENAMEMAXLEN * 2];
			 * System.arraycopy(bName,0, nameBuffer,0,
			 * Math.min(bName.length,nameBuffer.length));
			 * System.arraycopy(nameBuffer, 0, buffer, i *
			 * sizeOfDirectoryEntry() + 5,nameBuffer.length );
			 */
		}

	}

	/**
	 * Write any modifications to the directory back to disk
	 *
	 * @param file
	 *            The file to contain the new directory contents.
	 */
	public void writeBack(OpenFile pFile) {
		int size = NachosFileSystem.NumDirEntries * sizeOfDirectoryEntry() + 4;
		byte[] buffer = new byte[size];

		JavaSys.intToBytes(mTableSize, buffer, 0);

		// (void) file->ReadAt((char *)table, tableSize *
		// sizeof(DirectoryEntry), 0);
		for (int i = 0; i < mTableSize; i++) {
			buffer[i * sizeOfDirectoryEntry() + 4] = (byte) (mTable[i].mInUse ? 1 : 0);
			if (mTable[i].mInUse) {
				JavaSys.intToBytes(mTable[i].mSector, buffer, i * sizeOfDirectoryEntry() + 5);
				String sName = new String(mTable[i].mName);
				for (int j = sName.length(); j < FILENAMEMAXLEN; j++) {
					sName += " ";
				}
				byte[] bName = sName.getBytes();
				byte[] nameBuffer = new byte[FILENAMEMAXLEN * 2];
				System.arraycopy(bName, 0, nameBuffer, 0, Math.min(bName.length, nameBuffer.length));
				System.arraycopy(nameBuffer, 0, buffer, i * sizeOfDirectoryEntry() + 9, nameBuffer.length);
			}
		}

		pFile.writeAt(buffer, buffer.length, 0);

	}

	/**
	 * Look up file name in directory, and return its location in the table of
	 * directory entries.
	 *
	 * @param pName
	 *            the file name to look up
	 * @return -1 if the name isn't in the directory, the location in the table
	 *         of directory entries where the file is saved.
	 */
	public int findIndex(String pName) {

		// Search through the directory table
		for (int i = 0; i < mTableSize; i++) {
			// if the entry is found
			if (mTable[i].mInUse && pName.equals(new String(mTable[i].mName))) {
				return i;
			}
		}

		// name not in directory
		return -1;
	}

	/**
	 * Look up file name in directory, and return the disk sector number where
	 * the file's header is stored. Return -1 if the name isn't in the
	 * directory.
	 *
	 * @param pName
	 *            the file name to look up
	 * @return the disk sector number where the file's header is stored. -1 if
	 *         the name isn't in the directory.
	 */
	public int find(String pName) {
		int i = findIndex(pName);
		Debug.print('f', "FileName : " + pName + "  Found at : " + i);

		if (i != -1) {
			return mTable[i].mSector;
		}
		return -1;
	}

	/**
	 * Add a file into the directory. Return TRUE if successful; return FALSE if
	 * the file name is already in the directory, or if the directory is
	 * completely full, and has no more space for additional file names.
	 *
	 * @param PName
	 *            the name of the file being added.
	 * @param pNewSector
	 *            the disk sector containing the added file's header.
	 * @return true if successful, false otherwise.
	 */
	public boolean add(String pName, int pNewSector) {
		if (pName.length() > 9) {
			assert (false);
		}

		if (findIndex(pName) != -1) {
			return false;
		}

		// Iterate through the table
		for (int i = 0; i < mTableSize; i++) {
			// If the entry is not inuse
			if (!mTable[i].mInUse) {
				// Mark it as in use
				mTable[i].mInUse = true;

				// strncpy(table[i].name, name, FileNameMaxLen);
				mTable[i].mName = pName.toCharArray();
				mTable[i].mSector = pNewSector;
				return true;
			}
		}
		return false; // no space. Fix when we have extensible files.
	}

	/**
	 * Remove a file name from the directory.
	 *
	 * @param pName
	 *            the file name to be removed
	 * @return true if successful, false if the file is not in the directory.
	 */
	public boolean remove(String pName) {
		int i = findIndex(pName);

		// name not in directory
		if (i == -1) {
			return false;
		}

		mTable[i].mInUse = false;
		return true;
	}

	/**
	 * List all the file names in the directory.
	 */
	public void list() {
		System.out.println("Printing");

		for (int i = 0; i < mTableSize; i++) {
			if (mTable[i].mInUse) {
				System.out.println(new String(mTable[i].mName));
			}
		}

	}

	/**
	 * List all the file names in the directory, their FileHeader locations, and
	 * the contents of each file. For debugging.
	 */
	public void print() {
		FileHeader hdr = new FileHeader();

		System.out.println("Directory contents:\n");

		for (int i = 0; i < mTableSize; i++) {
			if (mTable[i].mInUse) {
				System.out.println("Name: " + new String(mTable[i].mName) + ", Sector: " + mTable[i].mSector);
				hdr.fetchFrom(mTable[i].mSector);
				hdr.print();
			}
		}
		// delete hdr;
	}
}
