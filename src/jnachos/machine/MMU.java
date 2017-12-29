/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

import jnachos.kern.*;

/**
 * NOTE: the hardware translation of virtual addresses in the user program to
 * physical addresses (relative to the beginning of "mainMemory") can be
 * controlled by one of: a traditional linear page table a software-loaded
 * translation lookaside buffer (tlb) -- a cache of mappings of virtual page #'s
 * to physical page #'s
 * 
 * If "tlb" is null, the linear page table is used If "tlb" is non-null, the
 * Nachos kernel is responsible for managing the contents of the TLB. But the
 * kernel can use any data structure it wants (eg, segmented paging) for
 * handling TLB cache misses.
 * 
 * For simplicity, both the page table pointer and the TLB pointer are public.
 * However, while there can be multiple page tables (one per address space,
 * stored in memory), there is only one TLB (implemented in hardware). Thus the
 * TLB pointer should be considered as *read-only*, although the contents of the
 * TLB are free to be modified by the kernel software.
 */
public class MMU {

	/**
	 * The translation LookAside buffer this pointer should be considered
	 * "read-only" to Nachos kernel code.
	 */
	public static TranslationEntry[] mTlb;

	/** The page table used by the currently running process. */
	public static TranslationEntry mPageTable[];

	/** The size of the current page table. */
	public static int mPageTableSize;

	/** States whether or not to used the TLB. */
	public static boolean mUSE_TLB;

	/** The size of a page. The same as a disk sector for simplicity. */
	public static final int PageSize = 128;

	/**
	 * ??
	 */
	public MMU() {

	}

	/**
	 * Translate a virtual address into a physical address, using either a page
	 * table or a TLB. Check for alignment and all sorts of other errors, and if
	 * everything is ok, set the use/dirty bits in the translation table entry,
	 * and store the translated physical address in "physAddr". If there was an
	 * error, returns the type of the exception.
	 * 
	 * @param virtAddr
	 *            the virtual address to translate
	 * @param physAddr
	 *            the place to store the physical address
	 * @param size
	 *            the amount of memory being read or written
	 * @param writing
	 *            if TRUE, check the "read-only" bit in the TLB
	 */
	public static ExceptionType translate(int virtAddr, int[] physAddr, int size, boolean writing) {
		int i = 0;
		int vpn, offset;
		TranslationEntry entry;
		int pageFrame;

		Debug.print('a', "Translate 0x" + Integer.toHexString(virtAddr) + ", writing " + (writing ? "write" : "read"));

		// check for alignment errors
		if (((size == 4) && ((virtAddr & 0x3) != 0)) || ((size == 2) && ((virtAddr & 0x1) != 0))) {
			Debug.print('a', "alignment problem at " + virtAddr + ", size " + size);
			return ExceptionType.AddressErrorException;
		}

		// we must have either a TLB or a page table, but not both!
		assert (mTlb == null || mPageTable == null);
		assert (mTlb != null || mPageTable != null);

		// calculate the virtual page number, and offset within the page,
		// from the virtual address
		vpn = (int) virtAddr / PageSize;
		offset = (int) virtAddr % PageSize;

		if (mTlb == null) { // => page table => vpn is index into table
			if (vpn >= mPageTableSize) {
				Debug.print('a', "virtual page # " + virtAddr + " too large for page table size " + mPageTableSize);
				return ExceptionType.AddressErrorException;
			} else if (!mPageTable[vpn].valid) {
				Debug.print('a', "virtual page # " + virtAddr + "  too large for page table size " + mPageTableSize);
				return ExceptionType.PageFaultException;
			}

			entry = mPageTable[vpn];
		}

		else {
			for (entry = null, i = 0; i < Machine.TLBSize; i++) {
				if (mTlb[i].valid && (mTlb[i].virtualPage == vpn)) {
					entry = mTlb[i]; // FOUND!
					break;
				}
			}

			if (entry == null) { // not found
				Debug.print('a', "*** no valid TLB entry found for this virtual page!\n");
				return ExceptionType.PageFaultException; // really, this is a
															// TLB fault,
				// the page may be in memory,
				// but not in the TLB
			}
		}

		if (entry.readOnly && writing) { // trying to write to a read-only page
			Debug.print('a', virtAddr + " mapped read-only at " + i + " in TLB!\n");
			return ExceptionType.ReadOnlyException;
		}

		pageFrame = entry.physicalPage;

		// if the pageFrame is too big, there is something really wrong!
		// An invalid translation was loaded into the page table or TLB.
		if (pageFrame >= Machine.NumPhysPages) {
			Debug.print('a', "*** frame " + pageFrame + " > " + Machine.NumPhysPages);
			return ExceptionType.BusErrorException;
		}

		entry.use = true; // set the use, dirty bits

		if (writing) {
			entry.dirty = true;
		}

		physAddr[0] = pageFrame * PageSize + offset;

		assert ((physAddr[0] >= 0) && ((physAddr[0] + size) <= Machine.MemorySize));
		Debug.print('a', "phys addr = 0x" + Integer.toHexString(physAddr[0]));

		return ExceptionType.NoException;
	}
}
