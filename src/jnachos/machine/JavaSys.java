/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import jnachos.kern.Debug;

/**
 * JavaSys is a single point of entry to the java calls which access the
 * underlying operating system and File-System.
 */
public class JavaSys {
	/** The number of files that are open. */
	public static int mFileCount = 0;

	/** The hash table matches integers to files. */
	public static Hashtable<Integer, RandomAccessFile> mOpenFiles = new Hashtable<Integer, RandomAccessFile>();

	/**
	 * Open a file for writing. Create it if it doesn't exist; truncate it if it
	 * does already exist. Return the file descriptor.
	 * 
	 * @param pName
	 *            the file name
	 * @return The file index for the opened file.
	 */
	public static int openForWrite(String pName) {
		int fd = -1;
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(pName), "rwd");
			mOpenFiles.put(new Integer(mFileCount), raf);
			assert (raf != null);
			if (raf != null) {
				fd = mFileCount;
			}

			Debug.print('j', "File Opened:" + raf);

			mFileCount++;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return fd;
	}

	/**
	 * Open a file for reading or writing. Return the file descriptor, or error
	 * if it doesn't exist.
	 *
	 * @param name
	 *            the file name.
	 */
	public static int openForReadWrite(String pName, boolean crashOnError) {
		int fd = -1;
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(pName), "r");
			assert (raf != null);
			raf.close();

			raf = new RandomAccessFile(new File(pName), "rwd");

			if (raf != null) {
				fd = mFileCount;
				mOpenFiles.put(new Integer(fd), raf);
			}

			Debug.print('f', "File Opened:" + raf + "\t" + fd + "\t" + mOpenFiles);

			mFileCount++;
		} catch (Exception e) {
			return -1;
		}
		return fd;
	}

	/**
	 *
	 */
	public static Integer bytesToInt(byte[] bytes, int start) {
		return new Integer((bytes[start] << 24) + ((bytes[start + 1] & 0xFF) << 16) + ((bytes[start + 2] & 0xFF) << 8)
				+ (bytes[start + 3] & 0xFF));
	}

	/**
	 *
	 */
	public static void intToBytes(int value, byte[] bytes, int start) {

		bytes[start] = (byte) (value >>> 24);
		bytes[start + 1] = (byte) (value >>> 16);
		bytes[start + 2] = (byte) (value >>> 8);
		bytes[start + 3] = (byte) (value);

	}

	/**
	 * Read byte acters from an open file. Abort if read fails.
	 * 
	 * @param fd
	 *            the file descriptor of the file to read from.
	 * @param buffer
	 *            the data to read from the file.
	 * @param nBytes
	 *            the number of bytes to read.
	 * 
	 */
	public static void read(int fd, byte[] buffer, int nBytes) {
		try {
			RandomAccessFile raf = mOpenFiles.get(new Integer(fd));

			int retVal = raf.read(buffer);

			assert (retVal == nBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read byteacters from an open file, returning as many as are available.
	 * 
	 * @param fd
	 *            the file descriptor of the file to read from.
	 * @param buffer
	 *            the data to read from the file.
	 * @param nBytes
	 *            the number of bytes to read.
	 */
	public static int readPartial(int fd, byte[] buffer, int nBytes) {
		int retVal = -1;
		try {
			assert (buffer.length == nBytes);
			RandomAccessFile raf = mOpenFiles.get(new Integer(fd));
			retVal = raf.read(buffer);

			Debug.print('j', "length:" + raf.length());
			// assert(retVal == nBytes);
			Debug.print('j', "Bytes Read:" + retVal + "\t" + nBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retVal;

	}

	/**
	 * Write byteacters to an open file. Abort if write fails.
	 * 
	 * @param fd
	 *            the file descriptor of the file to write to.
	 * @param buffer
	 *            the data to write to the file.
	 * @param nBytes
	 *            the number of bytes to write.
	 */
	public static void writeFile(int fd, byte[] buffer, int nBytes) {
		try {
			RandomAccessFile raf = mOpenFiles.get(new Integer(fd));
			raf.write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the location within an open file. Abort on error.
	 * 
	 * @param fd
	 *            the file descriptor for the file.
	 * @param offset
	 *            the position to seek to.
	 */
	public static void lseek(int fd, int offset) {
		try {
			RandomAccessFile raf = mOpenFiles.get(new Integer(fd));
			raf.seek(offset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Report the current location within an open file.
	 * 
	 * @param fd
	 *            the relevant file descriptor.
	 */
	public static long tell(int fd) {

		RandomAccessFile raf = mOpenFiles.get(new Integer(fd));

		if (raf == null)
			return -1;
		try {
			return raf.getFilePointer();
		} catch (Exception e) {
		}

		return -1;
	}

	/**
	 * Close a file. Abort on error.
	 * 
	 * @param fd
	 *            the file descriptor of the file to close.
	 */
	public static void close(int fd) {
		try {
			RandomAccessFile raf = mOpenFiles.get(new Integer(fd));
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
