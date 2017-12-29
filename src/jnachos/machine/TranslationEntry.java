/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

/**
 * The following class defines an entry in a translation table -- either in a
 * page table or a TLB. Each entry defines a mapping from one virtual page to
 * one physical page. In addition, there are some extra bits for access control
 * (valid and read-only) and some bits for usage information (use and dirty).
 */
public class TranslationEntry {
	/** The page number in virtual memory. */
	public int virtualPage;

	/** The page number in real memory (relative to the start of main memory) */
	public int physicalPage;

	/**
	 * If this bit is set, the translation is ignored. (In other words, the
	 * entry hasn't been initialized.)
	 */
	public boolean valid;

	/**
	 * If this bit is set, the user program is not allowed to modify the
	 * contents of the page.
	 */
	public boolean readOnly;

	/**
	 * This bit is set by the hardware every time the page is referenced or
	 * modified.
	 */
	public boolean use;

	/**
	 * This bit is set by the hardware every time the page is modified.
	 */
	public boolean dirty;
}
