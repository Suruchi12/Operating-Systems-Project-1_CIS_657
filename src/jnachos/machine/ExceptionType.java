/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

/**
 * Enumerate the different types of exceptions that the Jnachos machine can
 * raise.
 *
 * NoException: Everything ok! SyscallException: A program executed a system
 * call. PageFaultException: No valid translation found ReadOnlyException: Write
 * attempted to page marked "read-only" BusErrorException: Translation resulted
 * in an invalid physical address AddressErrorException: Unaligned reference or
 * one that was beyond the end of the address space OverflowException: Integer
 * overflow in add or sub. IllegalInstrException: Unimplemented or reserved
 * instr.
 *
 */
public enum ExceptionType {
	NoException, // Everything ok!
	SyscallException, // A program executed a system call.
	PageFaultException, // No valid translation found
	ReadOnlyException, // Write attempted to page marked
	// "read-only"
	BusErrorException, // Translation resulted in an
	// invalid physical address
	AddressErrorException, // Unaligned reference or one that
	// was beyond the end of the
	// address space
	OverflowException, // Integer overflow in add or sub.
	IllegalInstrException // Unimplemented or reserved instr.
}
