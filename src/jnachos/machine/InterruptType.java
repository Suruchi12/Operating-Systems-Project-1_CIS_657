/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

/**
 * IntType records which hardware device generated an interrupt. In JNachos, we
 * support a hardware timer device, a disk, a console display and keyboard, and
 * a network.
 */
public enum InterruptType {
	TimerInt, DiskInt, ConsoleWriteInt, ConsoleReadInt, NetworkSendInt, NetworkRecvInt
}
