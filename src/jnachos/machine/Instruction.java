/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.machine;

/**
 * The following class defines an instruction, represented in both undecoded
 * binary form decoded to identify operation to do registers to act on any
 * immediate operand value.
 */
public class Instruction {
	/** binary representation of the instruction */
	public int value;

	/**
	 * Type of instruction. This is NOT the same as the opcode field from the
	 * instruction: see defs in mips.h
	 */
	public char opCode;

	/** Three registers from instruction. */
	public char rs, rt, rd;

	/**
	 * Immediate or target or shamt field or offset. Immediates are
	 * sign-extended.
	 */
	public int extra;

	/**
	 * Decode a MIPS instruction
	 */
	public Instruction decode() {
		OpInfo opPtr;

		rs = (char) ((value >> 21) & 0x1f);
		rt = (char) ((value >> 16) & 0x1f);
		rd = (char) ((value >> 11) & 0x1f);
		opPtr = MipsSim.opTable[(value >> 26) & 0x3f];
		opCode = (char) opPtr.opCode;

		if (opPtr.format == MipsSim.IFMT) {
			extra = value & 0xffff;
			if ((extra & 0x8000) != 0) {
				extra |= 0xffff0000;
			}
		} else if (opPtr.format == MipsSim.RFMT) {
			extra = (value >> 6) & 0x1f;
		} else {
			extra = value & 0x3ffffff;
		}
		if (opCode == MipsSim.SPECIAL) {
			opCode = (char) MipsSim.specialTable[value & 0x3f];
		} else if (opCode == MipsSim.BCOND) {
			int i = value & 0x1f0000;

			if (i == 0) {
				opCode = MipsSim.OP_BLTZ;
			} else if (i == 0x10000) {
				opCode = MipsSim.OP_BGEZ;
			} else if (i == 0x100000) {
				opCode = MipsSim.OP_BLTZAL;
			} else if (i == 0x110000) {
				opCode = MipsSim.OP_BGEZAL;
			} else {
				opCode = MipsSim.OP_UNIMP;
			}
		}

		return this;
	}
}
