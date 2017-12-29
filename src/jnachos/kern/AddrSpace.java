/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *
 *  Created by Patrick McSweeney on 12/6/08.
 */
package jnachos.kern;

import jnachos.machine.*;
import jnachos.userbin.NoffHeader;
import jnachos.filesystem.*;
import java.util.Arrays;

/**
 * Routines to manage address spaces (executing user programs).
 */
public class AddrSpace {
	/**
	 * The page table for the process.
	 */
	private TranslationEntry[] mPageTable;

	/**
	 * The number of pages in this address space.
	 */
	private int mNumPages;

	/**
	 * Defines how large a user stack is. This maybe increased as necessary.
	 */
	public static final int UserStackSize = 1024;

	public static BitMap mFreeMap = new BitMap(Machine.NumPhysPages);

	/**
	 * Do little endian to big endian conversion on the bytes in the object file
	 * header, in case the file was generated on a little endian machine, and
	 * we're now running on a big endian machine.
	 **/
	public static void swapHeader(NoffHeader noffH) {
		noffH.noffMagic = MipsSim.wordToHost(noffH.noffMagic);
		noffH.code.size = MipsSim.wordToHost(noffH.code.size);
		noffH.code.virtualAddr = MipsSim.wordToHost(noffH.code.virtualAddr);
		noffH.code.inFileAddr = MipsSim.wordToHost(noffH.code.inFileAddr);
		noffH.initData.size = MipsSim.wordToHost(noffH.initData.size);
		noffH.initData.virtualAddr = MipsSim.wordToHost(noffH.initData.virtualAddr);
		noffH.initData.inFileAddr = MipsSim.wordToHost(noffH.initData.inFileAddr);
		noffH.uninitData.size = MipsSim.wordToHost(noffH.uninitData.size);
		noffH.uninitData.virtualAddr = MipsSim.wordToHost(noffH.uninitData.virtualAddr);
		noffH.uninitData.inFileAddr = MipsSim.wordToHost(noffH.uninitData.inFileAddr);
	}

	/**
	 * Create an address space to run a user program. Load the program from a
	 * file "executable", and set everything up so that we can start executing
	 * user instructions.
	 *
	 * Assumes that the object code file is in NOFF format.
	 *
	 * First, set up the translation from program memory to physical memory. For
	 * now, this is really simple (1:1), since we are only uniprogramming, and
	 * we have a single unsegmented page table
	 *
	 * @param executable
	 *            is the file containing the object code to load into memory
	 **/
	public AddrSpace(OpenFile executable) {
		// Create buffer to hold onto the noff header
		byte[] buffer = new byte[NoffHeader.size];

		// Read in the buffer
		executable.readAt(buffer, NoffHeader.size, 0);

		// Create a Noff Header with the data we read in
		NoffHeader noffH = new NoffHeader(buffer);

		// Check to see if the headers match
		if ((noffH.noffMagic != NoffHeader.NOFFMAGIC)
				&& (MipsSim.wordToHost(noffH.noffMagic) == NoffHeader.NOFFMAGIC)) {
			// If so swap the headers
			swapHeader(noffH);
		}

		// Make sure that the magic numbers match
		assert (noffH.noffMagic == NoffHeader.NOFFMAGIC);

		// how big is address space?
		int size = noffH.code.size + noffH.initData.size + noffH.uninitData.size + UserStackSize;

		Debug.print('a', "File Size:" + size);

		// Calculate the number of pages
		mNumPages = (int) Math.ceil((double) size / (double) Machine.PageSize);

		// Recalculate based on the number of pages
		size = mNumPages * Machine.PageSize;

		// check we're not trying to run anything too big --
		// at least until we have virtual memory
		assert (mNumPages <= Machine.NumPhysPages);

		Debug.print('a', "Initializing address space, num pages " + mNumPages + ", size " + size);

		// first, set up the translation
		mPageTable = new TranslationEntry[mNumPages];
		for (int i = 0; i < mNumPages; i++) {
			mPageTable[i] = new TranslationEntry();
			mPageTable[i].virtualPage = i;
			mPageTable[i].physicalPage = mFreeMap.find();
			mPageTable[i].valid = true;
			mPageTable[i].use = false;
			mPageTable[i].dirty = false;

			// if the code segment was entirely on
			mPageTable[i].readOnly = false;
			// a separate page, we could set its
			// pages to be read-only

			// Zero out all of main memory
			Arrays.fill(Machine.mMainMemory, mPageTable[i].physicalPage * Machine.PageSize,
					(mPageTable[i].physicalPage + 1) * Machine.PageSize, (byte) 0);

			// Copy the code segment into memory
			if ((i * Machine.PageSize) < (noffH.code.size + noffH.initData.size)) {
				Debug.print('a',
						"Initializing code segment, at " + noffH.code.virtualAddr + ", size " + noffH.code.size);

				// Create a temporary buffer to copy the code
				byte[] bytes = new byte[Machine.PageSize];

				// read the code into the buffer
				executable.readAt(bytes, Machine.PageSize, noffH.code.inFileAddr + i * Machine.PageSize);

				// Copy the buffer into the main memory
				System.arraycopy(bytes, 0, Machine.mMainMemory, mPageTable[i].physicalPage * Machine.PageSize,
						Machine.PageSize);
			}
		}
	}

	/**
	 * 
	 * @param pToCopy
	 */
	public AddrSpace(AddrSpace pToCopy) {

		// Calculate the number of pages
		mNumPages = pToCopy.mNumPages;

		// check we're not trying to run anything too big --
		// at least until we have virtual memory
		assert (mNumPages <= Machine.NumPhysPages);

		// first, set up the translation
		mPageTable = new TranslationEntry[mNumPages];
		for (int i = 0; i < mNumPages; i++) {
			mPageTable[i] = new TranslationEntry();
			mPageTable[i].virtualPage = i;
			mPageTable[i].physicalPage = mFreeMap.find();
			mPageTable[i].valid = true;
			mPageTable[i].use = false;
			mPageTable[i].dirty = false;

			// if the code segment was entirely on
			mPageTable[i].readOnly = false;
			// a separate page, we could set its
			// pages to be read-only

			// Zero out all of main memory
			Arrays.fill(Machine.mMainMemory, mPageTable[i].physicalPage * Machine.PageSize,
					(mPageTable[i].physicalPage + 1) * Machine.PageSize, (byte) 0);

			// Copy the buffer into the main memory
			System.arraycopy(Machine.mMainMemory, pToCopy.mPageTable[i].physicalPage * Machine.PageSize,
					Machine.mMainMemory, mPageTable[i].physicalPage * Machine.PageSize, Machine.PageSize);
		}
	}

	/**
	 * Set the initial values for the user-level register set.
	 *
	 * We write these directly into the "machine" registers, so that we can
	 * immediately jump to user code. Note that these will be saved/restored
	 * into the currentThread.userRegisters when this thread is context switched
	 * out.
	 */
	public void initRegisters() {
		// Set all of the registers to 0
		for (int i = 0; i < Machine.NumTotalRegs; i++) {
			Machine.writeRegister(i, 0);
		}

		// Initial program counter -- must be location of "Start"
		Machine.writeRegister(Machine.PCReg, 0);

		// Need to also tell MIPS where next instruction is, because
		// of branch delay possibility
		Machine.writeRegister(Machine.NextPCReg, 4);

		// Set the stack register to the end of the address space, where we
		// allocated the stack; but subtract off a bit, to make sure we don't
		// accidentally reference off the end!
		Machine.writeRegister(Machine.StackReg, mNumPages * Machine.PageSize - 16);

		Debug.print('a', "Initializing stack register to " + (mNumPages * Machine.PageSize - 16));
	}

	/**
	 * On a context switch, save any machine state, specific to this address
	 * space, that needs saving.
	 *
	 **/
	public void saveState() {

	}

	/**
	 * On a context switch, restore the machine state so that this address space
	 * can run.
	 *
	 * For now, tell the machine where to find the page table.
	 */
	public void restoreState() {
		MMU.mPageTable = mPageTable;
		MMU.mPageTableSize = mNumPages;
	}
}
