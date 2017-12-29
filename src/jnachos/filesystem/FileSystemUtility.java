// fstest.cc 
//	Simple test routines for the file system.  
//
//	We implement:
//	   Copy -- copy a file from UNIX to Nachos
//	   Print -- cat the contents of a Nachos file 
//	   Perftest -- a stress test for the Nachos file system
//		read and write a really large file in tiny chunks
//		(won't work on baseline system!)
//
// Copyright (c) 1992-1993 The Regents of the University of California.
// All rights reserved.  See copyright.h for copyright notice and limitation 
// of liability and disclaimer of warranty provisions.
package jnachos.filesystem;

import jnachos.kern.*;
import java.io.*;

public class FileSystemUtility {

	// make it small, just to be difficult
	private static final int TransferSize = 10;

	/**
	 * Copy the contents of the UNIX file "from" to the Nachos file "to"
	 * 
	 * @param from
	 *            The Java File System file to read
	 * @param to
	 *            The Nachos File System to read
	 */
	public static void copy(String from, String to) {
		try {
			RandomAccessFile in = new RandomAccessFile(from, "r");

			int amountRead = 0;

			// Figure out length of UNIX file
			long fileLength = in.length();

			// Create a Nachos file of the same length
			Debug.print('f', "Copying file " + from + ", size " + fileLength + " to file " + to);

			// Create Nachos file
			if (!JNachos.mFileSystem.create(to, (int) fileLength)) {
				System.out.println("Copy: couldn't create output file " + to);
				in.close();
				return;
			}

			NachosOpenFile openFile = (NachosOpenFile) JNachos.mFileSystem.open(to);
			assert (openFile != null);

			System.out.println(openFile);

			// Copy the data in TransferSize chunks
			byte[] buffer = new byte[TransferSize];
			while ((amountRead = in.read(buffer, 0, TransferSize)) > 0) {
				openFile.write(buffer, amountRead);

			}

			// Close the UNIX and the Nachos files
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Print the contents of the Nachos file "name".
	 */
	public static void print(String pFileName) {
		NachosOpenFile openFile = null;

		if ((openFile = (NachosOpenFile) JNachos.mFileSystem.open(pFileName)) == null) {
			System.out.println("Print: unable to open file " + pFileName);
			return;
		}

		byte[] buffer = new byte[TransferSize];
		int amountRead = 0;
		while ((amountRead = openFile.read(buffer, TransferSize)) > 0) {
			String s = new String(buffer);
			System.out.print(s);
		}

		return;
	}

}
/*
 * //---------------------------------------------------------------------- //
 * PerformanceTest // Stress the Nachos file system by creating a large file,
 * writing // it out a bit at a time, reading it back a bit at a time, and then
 * // deleting the file. // // Implemented as three separate routines: //
 * FileWrite -- write the file // FileRead -- read the file // PerformanceTest
 * -- overall control, and print out performance #'s
 * //----------------------------------------------------------------------
 * 
 * #define FileName "TestFile" #define Contents "1234567890" #define ContentSize
 * strlen(Contents) #define FileSize ((int)(ContentSize * 5000))
 * 
 * static void FileWrite() { OpenFile *openFile; int i, numBytes;
 * 
 * printf("Sequential write of %d byte file, in %d byte chunks\n", FileSize,
 * ContentSize); if (!fileSystem->Create(FileName, 0)) {
 * printf("Perf test: can't create %s\n", FileName); return; } openFile =
 * fileSystem->Open(FileName); if (openFile == NULL) {
 * printf("Perf test: unable to open %s\n", FileName); return; } for (i = 0; i <
 * FileSize; i += ContentSize) { numBytes = openFile->Write(Contents,
 * ContentSize); if (numBytes < 10) { printf("Perf test: unable to write %s\n",
 * FileName); delete openFile; return; } } delete openFile; // close file }
 * 
 * static void FileRead() { OpenFile *openFile; char *buffer = new
 * char[ContentSize]; int i, numBytes;
 * 
 * printf("Sequential read of %d byte file, in %d byte chunks\n", FileSize,
 * ContentSize);
 * 
 * if ((openFile = fileSystem->Open(FileName)) == NULL) {
 * printf("Perf test: unable to open file %s\n", FileName); delete buffer;
 * return; } for (i = 0; i < FileSize; i += ContentSize) { numBytes =
 * openFile->Read(buffer, ContentSize); if ((numBytes < 10) || strncmp(buffer,
 * Contents, ContentSize)) { printf("Perf test: unable to read %s\n", FileName);
 * delete openFile; delete buffer; return; } } delete buffer; delete openFile;
 * // close file }
 * 
 * void PerformanceTest() { printf("Starting file system performance test:\n");
 * stats->Print(); FileWrite(); FileRead(); if (!fileSystem->Remove(FileName)) {
 * printf("Perf test: unable to remove %s\n", FileName); return; }
 * stats->Print(); }
 * 
 */