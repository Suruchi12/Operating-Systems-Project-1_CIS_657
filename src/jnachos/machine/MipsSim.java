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
 * OpInfo maintains information about instructions in the MIPS assembly
 * language.
 * 
 */
class OpInfo {
	/** Translated OP code. */
	public int opCode;

	/** Format type (IFMT or JFMT or RFMT). */
	public int format;

	public OpInfo(int pOpCode, int pFormat) {
		opCode = pOpCode;
		format = pFormat;
	}
}

/**
 * OpInfo maintains information about instructions in the MIPS assembly
 * language.
 */
class OpString {
	/** Printed version of instruction. */
	public String string;

	/** The first possible register used by the instruction. */
	public MipsSim.RegType mA;

	/** The second possible register used by the instruction. */
	public MipsSim.RegType mB;

	/** The third possible register used by the instruction. */
	public MipsSim.RegType mC;

	/**
	 * OpString constructor.
	 * 
	 * @param pString
	 *            the printed version of the string.
	 * @param pA
	 *            the first register for the instruction.
	 * @param pB
	 *            the second register for the instruction.
	 * @param pC
	 *            the third register for the instruction.
	 */
	public OpString(String pString, MipsSim.RegType pA, MipsSim.RegType pB, MipsSim.RegType pC) {
		string = pString;
		mA = pA;
		mB = pB;
		mC = pC;
	}

}

/**
 * 
 * MipsSim does all of the emulation of the JNachos CPU.
 *
 */
public class MipsSim {

	enum RegType {
		NONE, RS, RT, RD, EXTRA
	};

	/*
	 * OpCode values. The names are straight from the MIPS manual except for the
	 * following special ones:
	 *
	 * OP_UNIMP - means that this instruction is legal, but hasn't been
	 * implemented in the simulator yet. OP_RES - means that this is a reserved
	 * opcode (it isn't supported by the architecture).
	 */
	public static final int OP_ADD = 1;
	public static final int OP_ADDI = 2;
	public static final int OP_ADDIU = 3;
	public static final int OP_ADDU = 4;
	public static final int OP_AND = 5;
	public static final int OP_ANDI = 6;
	public static final int OP_BEQ = 7;
	public static final int OP_BGEZ = 8;
	public static final int OP_BGEZAL = 9;
	public static final int OP_BGTZ = 10;
	public static final int OP_BLEZ = 11;
	public static final int OP_BLTZ = 12;
	public static final int OP_BLTZAL = 13;
	public static final int OP_BNE = 14;
	public static final int OP_DIV = 16;
	public static final int OP_DIVU = 17;
	public static final int OP_J = 18;
	public static final int OP_JAL = 19;
	public static final int OP_JALR = 20;
	public static final int OP_JR = 21;
	public static final int OP_LB = 22;
	public static final int OP_LBU = 23;
	public static final int OP_LH = 24;
	public static final int OP_LHU = 25;
	public static final int OP_LUI = 26;
	public static final int OP_LW = 27;
	public static final int OP_LWL = 28;
	public static final int OP_LWR = 29;
	public static final int OP_MFHI = 31;
	public static final int OP_MFLO = 32;
	public static final int OP_MTHI = 34;
	public static final int OP_MTLO = 35;
	public static final int OP_MULT = 36;
	public static final int OP_MULTU = 37;
	public static final int OP_NOR = 38;
	public static final int OP_OR = 39;
	public static final int OP_ORI = 40;
	public static final int OP_RFE = 41;
	public static final int OP_SB = 42;
	public static final int OP_SH = 43;
	public static final int OP_SLL = 44;
	public static final int OP_SLLV = 45;
	public static final int OP_SLT = 46;
	public static final int OP_SLTI = 47;
	public static final int OP_SLTIU = 48;
	public static final int OP_SLTU = 49;
	public static final int OP_SRA = 50;
	public static final int OP_SRAV = 51;
	public static final int OP_SRL = 52;
	public static final int OP_SRLV = 53;
	public static final int OP_SUB = 54;
	public static final int OP_SUBU = 55;
	public static final int OP_SW = 56;
	public static final int OP_SWL = 57;
	public static final int OP_SWR = 58;
	public static final int OP_XOR = 59;
	public static final int OP_XORI = 60;
	public static final int OP_SYSCALL = 61;
	public static final int OP_UNIMP = 62;
	public static final int OP_RES = 63;
	public static final int MaxOpcode = 63;

	/*
	 * Miscellaneous definitions:
	 */

	public static int indexToAddr(int x) {
		return (x << 2);
	}

	public static final int SIGN_BIT = 0x80000000;
	public static final int R31 = 31;

	/*
	 * The table below is used to translate bits 31:26 of the instruction into a
	 * value suitable for the "opCode" field of a MemWord structure, or into a
	 * special value for further decoding.
	 */

	public static final int SPECIAL = 100;
	public static final int BCOND = 101;

	public static final int IFMT = 1;
	public static final int JFMT = 2;
	public static final int RFMT = 3;

	public static OpInfo[] opTable = { new OpInfo(SPECIAL, RFMT), new OpInfo(BCOND, IFMT), new OpInfo(OP_J, JFMT),
			new OpInfo(OP_JAL, JFMT), new OpInfo(OP_BEQ, IFMT), new OpInfo(OP_BNE, IFMT), new OpInfo(OP_BLEZ, IFMT),
			new OpInfo(OP_BGTZ, IFMT), new OpInfo(OP_ADDI, IFMT), new OpInfo(OP_ADDIU, IFMT), new OpInfo(OP_SLTI, IFMT),
			new OpInfo(OP_SLTIU, IFMT), new OpInfo(OP_ANDI, IFMT), new OpInfo(OP_ORI, IFMT), new OpInfo(OP_XORI, IFMT),
			new OpInfo(OP_LUI, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT),
			new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_LB, IFMT), new OpInfo(OP_LH, IFMT),
			new OpInfo(OP_LWL, IFMT), new OpInfo(OP_LW, IFMT), new OpInfo(OP_LBU, IFMT), new OpInfo(OP_LHU, IFMT),
			new OpInfo(OP_LWR, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_SB, IFMT), new OpInfo(OP_SH, IFMT),
			new OpInfo(OP_SWL, IFMT), new OpInfo(OP_SW, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_SWR, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT),
			new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT),
			new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_UNIMP, IFMT), new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT),
			new OpInfo(OP_RES, IFMT), new OpInfo(OP_RES, IFMT) };

	/*
	 * The table below is used to convert the "funct" field of SPECIAL
	 * instructions into the "opCode" field of a MemWord.
	 */

	public static int specialTable[] = { OP_SLL, OP_RES, OP_SRL, OP_SRA, OP_SLLV, OP_RES, OP_SRLV, OP_SRAV, OP_JR,
			OP_JALR, OP_RES, OP_RES, OP_SYSCALL, OP_UNIMP, OP_RES, OP_RES, OP_MFHI, OP_MTHI, OP_MFLO, OP_MTLO, OP_RES,
			OP_RES, OP_RES, OP_RES, OP_MULT, OP_MULTU, OP_DIV, OP_DIVU, OP_RES, OP_RES, OP_RES, OP_RES, OP_ADD, OP_ADDU,
			OP_SUB, OP_SUBU, OP_AND, OP_OR, OP_XOR, OP_NOR, OP_RES, OP_RES, OP_SLT, OP_SLTU, OP_RES, OP_RES, OP_RES,
			OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES, OP_RES,
			OP_RES, OP_RES, OP_RES, OP_RES };

	// Stuff to help print out each instruction, for debugging

	public static OpString[] opStrings = {
			new OpString(new String("Shouldn't happen"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("ADD"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("ADDI"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("ADDIU"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("ADDU"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("AND"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("ANDI"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("BEQ"), RegType.RS, RegType.RT, RegType.EXTRA),
			new OpString(new String("BGEZ"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BGEZAL"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BGTZ"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BLEZ"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BLTZ"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BLTZAL"), RegType.RS, RegType.EXTRA, RegType.NONE),
			new OpString(new String("BNE"), RegType.RS, RegType.RT, RegType.EXTRA),
			new OpString(new String("Shouldn't happen"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("DIV"), RegType.RS, RegType.RT, RegType.NONE),
			new OpString(new String("DIVU"), RegType.RS, RegType.RT, RegType.NONE),
			new OpString(new String("J"), RegType.EXTRA, RegType.NONE, RegType.NONE),
			new OpString(new String("JAL"), RegType.EXTRA, RegType.NONE, RegType.NONE),
			new OpString(new String("JALR"), RegType.RD, RegType.RS, RegType.NONE),
			new OpString(new String("JR"), RegType.RD, RegType.RS, RegType.NONE),
			new OpString(new String("LB"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LBU"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LH"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LHU"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LUI"), RegType.RT, RegType.EXTRA, RegType.NONE),
			new OpString(new String("LW"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LWL"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("LWR"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("Shouldn't happen"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("MFHI"), RegType.RD, RegType.NONE, RegType.NONE),
			new OpString(new String("MFLO"), RegType.RD, RegType.NONE, RegType.NONE),
			new OpString(new String("Shouldn't happen"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("MTHI"), RegType.RS, RegType.NONE, RegType.NONE),
			new OpString(new String("MTLO"), RegType.RS, RegType.NONE, RegType.NONE),
			new OpString(new String("MULT"), RegType.RS, RegType.RT, RegType.NONE),
			new OpString(new String("MULTU"), RegType.RS, RegType.RT, RegType.NONE),
			new OpString(new String("NOR"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("OR"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("ORI"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("RFE"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("SB"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("SH"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("SLL"), RegType.RD, RegType.RT, RegType.EXTRA),
			new OpString(new String("SLLV"), RegType.RD, RegType.RT, RegType.RS),
			new OpString(new String("SLT"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("SLTI"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("SLTIU"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("SLTU"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("SRA"), RegType.RD, RegType.RT, RegType.EXTRA),
			new OpString(new String("SRAV"), RegType.RD, RegType.RT, RegType.RS),
			new OpString(new String("SRL"), RegType.RD, RegType.RT, RegType.EXTRA),
			new OpString(new String("SRLV"), RegType.RD, RegType.RT, RegType.RS),
			new OpString(new String("SUB"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("SUBU"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("SW"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("SWL"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("SWR"), RegType.RT, RegType.EXTRA, RegType.RS),
			new OpString(new String("XOR"), RegType.RD, RegType.RS, RegType.RT),
			new OpString(new String("XORI"), RegType.RT, RegType.RS, RegType.EXTRA),
			new OpString(new String("SYSCALL"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("Unimplemented"), RegType.NONE, RegType.NONE, RegType.NONE),
			new OpString(new String("Reserved"), RegType.NONE, RegType.NONE, RegType.NONE) };

	/**
	 * Retrieve the register # referred to in an instruction.
	 */
	public static int typeToReg(RegType reg, Instruction instr) {
		switch (reg) {
		case RS:
			return instr.rs;
		case RT:
			return instr.rt;
		case RD:
			return instr.rd;
		case EXTRA:
			return instr.extra;
		default:
			return -1;
		}
	}

	/**
	 * Execute one instruction from a user-level program
	 *
	 * If there is any kind of exception or interrupt, we invoke the exception
	 * handler, and when it returns, we return to Run(), which will re-invoke us
	 * in a loop. This allows us to re-start the instruction execution from the
	 * beginning, in case any of our state has changed. On a syscall, the OS
	 * software must increment the PC so execution begins at the instruction
	 * immediately after the syscall.
	 *
	 * This routine is re-entrant, in that it can be called multiple times
	 * concurrently -- one for each thread executing user code. We get
	 * re-entrancy by never caching any data -- we always re-start the
	 * simulation from scratch each time we are called (or after trapping back
	 * to the Nachos kernel on an exception or interrupt), and we always store
	 * all data back to the Machine Machine.mRegisters and memory before
	 * leaving. This allows the Nachos kernel to control our behavior by
	 * controlling the contents of memory, the translation table, and the
	 * register set.
	 */
	public static void oneInstruction(Instruction instr) {

		int nextLoadReg = 0;
		int nextLoadValue = 0; // record delayed load operation, to apply in the
								// future

		// Fetch instruction
		Integer raw = (Integer) Machine.readMem(Machine.mRegisters[Machine.PCReg], 4);
		if (raw == null) {
			return; // exception occurred
		}

		instr.value = raw.intValue();
		instr.decode();

		if (Debug.isEnabled('m')) {
			OpString str = opStrings[(char) instr.opCode];
			assert (instr.opCode <= MaxOpcode);
			Debug.print('a', "At PC = " + Integer.toHexString(Machine.mRegisters[Machine.PCReg]));
			Debug.print('p', "VAL: " + str.string + ", r" + typeToReg(str.mA, instr) + ", r" + typeToReg(str.mB, instr)
					+ "," + typeToReg(str.mC, instr));
			Debug.print('a', "\n");
		}

		// Compute next pc, but don't install in case there's an error or
		// branch.
		int pcAfter = Machine.mRegisters[Machine.NextPCReg] + 4;
		int sum, diff, tmp, value;
		int rs, rt, imm;
		Integer result;

		// Execute the instruction (cf. Kane's book)
		switch (instr.opCode) {

		case OP_ADD:
			sum = Machine.mRegisters[instr.rs] + Machine.mRegisters[instr.rt];
			if (!(((Machine.mRegisters[instr.rs] ^ Machine.mRegisters[instr.rt]) & SIGN_BIT) != 0)
					&& (((Machine.mRegisters[instr.rs] ^ sum) & SIGN_BIT) != 0)) {
				Machine.raiseException(ExceptionType.OverflowException, 0);
				return;
			}
			Machine.mRegisters[instr.rd] = sum;
			break;

		case OP_ADDI:
			sum = Machine.mRegisters[instr.rs] + instr.extra;
			if (!(((Machine.mRegisters[instr.rs] ^ instr.extra) & SIGN_BIT) != 0)
					&& (((instr.extra ^ sum) & SIGN_BIT) != 0)) {
				Machine.raiseException(ExceptionType.OverflowException, 0);
				return;
			}

			Machine.mRegisters[instr.rt] = sum;
			break;

		case OP_ADDIU:
			Machine.mRegisters[instr.rt] = Machine.mRegisters[instr.rs] + instr.extra;
			break;

		case OP_ADDU:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rs] + Machine.mRegisters[instr.rt];
			break;

		case OP_AND:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rs] & Machine.mRegisters[instr.rt];
			break;

		case OP_ANDI:
			Machine.mRegisters[instr.rt] = Machine.mRegisters[instr.rs] & (instr.extra & 0xffff);
			break;

		case OP_BEQ:
			if (Machine.mRegisters[instr.rs] == Machine.mRegisters[instr.rt]) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_BGEZAL:
			Machine.mRegisters[R31] = Machine.mRegisters[Machine.NextPCReg] + 4;

		case OP_BGEZ:
			if (!((Machine.mRegisters[instr.rs] & SIGN_BIT) != 0)) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_BGTZ:
			if (Machine.mRegisters[instr.rs] > 0) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_BLEZ:
			if (Machine.mRegisters[instr.rs] <= 0) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_BLTZAL:
			Machine.mRegisters[R31] = Machine.mRegisters[Machine.NextPCReg] + 4;

		case OP_BLTZ:
			if ((Machine.mRegisters[instr.rs] & SIGN_BIT) != 0) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_BNE:
			if (Machine.mRegisters[instr.rs] != Machine.mRegisters[instr.rt]) {
				pcAfter = Machine.mRegisters[Machine.NextPCReg] + indexToAddr(instr.extra);
			}
			break;

		case OP_DIV:
			if (Machine.mRegisters[instr.rt] == 0) {
				Machine.mRegisters[Machine.LoReg] = 0;
				Machine.mRegisters[Machine.HiReg] = 0;
			} else {
				Machine.mRegisters[Machine.LoReg] = Machine.mRegisters[instr.rs] / Machine.mRegisters[instr.rt];
				Machine.mRegisters[Machine.HiReg] = Machine.mRegisters[instr.rs] % Machine.mRegisters[instr.rt];
			}
			break;

		case OP_DIVU:
			rs = Machine.mRegisters[instr.rs];
			rt = Machine.mRegisters[instr.rt];
			if (rt == 0) {
				Machine.mRegisters[Machine.LoReg] = 0;
				Machine.mRegisters[Machine.HiReg] = 0;
			} else {
				tmp = rs / rt;
				Machine.mRegisters[Machine.LoReg] = (int) tmp;
				tmp = rs % rt;
				Machine.mRegisters[Machine.HiReg] = (int) tmp;
			}
			break;

		case OP_JAL:
			Machine.mRegisters[R31] = Machine.mRegisters[Machine.NextPCReg] + 4;

		case OP_J:
			pcAfter = (pcAfter & 0xf0000000) | indexToAddr(instr.extra);
			break;

		case OP_JALR:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[Machine.NextPCReg] + 4;

		case OP_JR:
			pcAfter = Machine.mRegisters[instr.rs];
			break;

		case OP_LB:
		case OP_LBU:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;
			result = Machine.readMem(tmp, 1);
			if (result == null) {
				return;
			}

			value = result.intValue();

			if (((value & 0x80) != 0) && (instr.opCode == OP_LB)) {
				value |= 0xffffff00;
			} else {
				value &= 0xff;
			}
			nextLoadReg = instr.rt;
			nextLoadValue = value;
			break;

		case OP_LH:
		case OP_LHU:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;
			if ((tmp & 0x1) != 0) {
				Machine.raiseException(ExceptionType.AddressErrorException, tmp);
				return;
			}
			result = Machine.readMem(tmp, 2);
			if (result == null) {
				return;
			}
			value = result.intValue();

			if (((value & 0x8000) != 0) && (instr.opCode == OP_LH)) {
				value |= 0xffff0000;
			} else {
				value &= 0xffff;
			}

			nextLoadReg = instr.rt;
			nextLoadValue = value;
			break;

		case OP_LUI:
			Debug.print('m', "Executing: LUI r" + instr.rt + ", " + instr.extra);
			Machine.mRegisters[instr.rt] = instr.extra << 16;
			break;

		case OP_LW:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;
			if ((tmp & 0x3) != 0) {
				Machine.raiseException(ExceptionType.AddressErrorException, tmp);
				return;
			}

			result = (Integer) Machine.readMem(tmp, 4);
			if (result == null) {
				return;
			}
			value = result.intValue();

			nextLoadReg = instr.rt;
			nextLoadValue = value;
			break;

		case OP_LWL:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;
			// readMem assumes all 4 byte requests are aligned on an even
			// word boundary. Also, the little endian/big endian swap code would
			// fail (I think) if the other cases are ever exercised.
			assert ((tmp & 0x3) == 0);

			result = (Integer) Machine.readMem(tmp, 4);
			if (result == null) {
				return;
			}
			value = result.intValue();

			if (Machine.mRegisters[Machine.LoadReg] == instr.rt) {
				nextLoadValue = Machine.mRegisters[Machine.LoadValueReg];
			} else {
				nextLoadValue = Machine.mRegisters[instr.rt];
			}

			switch (tmp & 0x3) {
			case 0:
				nextLoadValue = value;
				break;
			case 1:
				nextLoadValue = (nextLoadValue & 0xff) | (value << 8);
				break;
			case 2:
				nextLoadValue = (nextLoadValue & 0xffff) | (value << 16);
				break;
			case 3:
				nextLoadValue = (nextLoadValue & 0xffffff) | (value << 24);
				break;
			}

			nextLoadReg = instr.rt;
			break;

		case OP_LWR:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;

			// readMem assumes all 4 byte requests are aligned on an even
			// word boundary. Also, the little endian/big endian swap code would
			// fail (I think) if the other cases are ever exercised.
			assert ((tmp & 0x3) == 0);

			result = (Integer) Machine.readMem(tmp, 4);
			if (result == null) {
				return;
			}
			value = result.intValue();
			if (Machine.mRegisters[Machine.LoadReg] == instr.rt) {
				nextLoadValue = Machine.mRegisters[Machine.LoadValueReg];
			} else {
				nextLoadValue = Machine.mRegisters[instr.rt];
			}

			switch (tmp & 0x3) {
			case 0:
				nextLoadValue = (nextLoadValue & 0xffffff00) | ((value >> 24) & 0xff);
				break;
			case 1:
				nextLoadValue = (nextLoadValue & 0xffff0000) | ((value >> 16) & 0xffff);
				break;
			case 2:
				nextLoadValue = (nextLoadValue & 0xff000000) | ((value >> 8) & 0xffffff);
				break;
			case 3:
				nextLoadValue = value;
				break;
			}

			nextLoadReg = instr.rt;
			break;

		case OP_MFHI:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[Machine.HiReg];
			break;

		case OP_MFLO:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[Machine.LoReg];
			break;

		case OP_MTHI:
			Machine.mRegisters[Machine.HiReg] = Machine.mRegisters[instr.rs];
			break;

		case OP_MTLO:
			Machine.mRegisters[Machine.LoReg] = Machine.mRegisters[instr.rs];
			break;

		case OP_MULT:
			int[] result1 = new int[2];
			Mult(Machine.mRegisters[instr.rs], Machine.mRegisters[instr.rt], true, result1);
			Machine.mRegisters[Machine.HiReg] = result1[1];
			Machine.mRegisters[Machine.LoReg] = result1[0];
			break;

		case OP_MULTU:
			int[] result2 = new int[2];
			Mult(Machine.mRegisters[instr.rs], Machine.mRegisters[instr.rt], false, result2);
			Machine.mRegisters[Machine.HiReg] = result2[1];
			Machine.mRegisters[Machine.LoReg] = result2[0];
			break;

		case OP_NOR:
			Machine.mRegisters[instr.rd] = ~(Machine.mRegisters[instr.rs] | Machine.mRegisters[instr.rt]);
			break;

		case OP_OR:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rs] | Machine.mRegisters[instr.rs];
			break;

		case OP_ORI:
			Machine.mRegisters[instr.rt] = Machine.mRegisters[instr.rs] | (instr.extra & 0xffff);
			break;

		case OP_SB:
			if (!Machine.writeMem((Machine.mRegisters[instr.rs] + instr.extra), 1, Machine.mRegisters[instr.rt])) {
				return;
			}
			break;

		case OP_SH:
			if (!Machine.writeMem((Machine.mRegisters[instr.rs] + instr.extra), 2, Machine.mRegisters[instr.rt])) {
				return;
			}
			break;

		case OP_SLL:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rt] << instr.extra;
			break;

		case OP_SLLV:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rt] << (Machine.mRegisters[instr.rs] & 0x1f);
			break;

		case OP_SLT:
			if (Machine.mRegisters[instr.rs] < Machine.mRegisters[instr.rt]) {
				Machine.mRegisters[instr.rd] = 1;
			} else {
				Machine.mRegisters[instr.rd] = 0;
			}
			break;

		case OP_SLTI:
			if (Machine.mRegisters[instr.rs] < instr.extra) {
				Machine.mRegisters[instr.rt] = 1;
			} else {
				Machine.mRegisters[instr.rt] = 0;
			}
			break;

		case OP_SLTIU:
			rs = Machine.mRegisters[instr.rs];
			imm = instr.extra;
			if (rs < imm) {
				Machine.mRegisters[instr.rt] = 1;
			} else {
				Machine.mRegisters[instr.rt] = 0;
			}
			break;

		case OP_SLTU:
			rs = Machine.mRegisters[instr.rs];
			rt = Machine.mRegisters[instr.rt];
			if (rs < rt) {
				Machine.mRegisters[instr.rd] = 1;
			} else {
				Machine.mRegisters[instr.rd] = 0;
			}
			break;

		case OP_SRA:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rt] >> instr.extra;
			break;

		case OP_SRAV:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rt] >> (Machine.mRegisters[instr.rs] & 0x1f);
			break;

		case OP_SRL:
			tmp = Machine.mRegisters[instr.rt];
			tmp >>= instr.extra;
			Machine.mRegisters[instr.rd] = tmp;
			break;

		case OP_SRLV:
			tmp = Machine.mRegisters[instr.rt];
			tmp >>= (Machine.mRegisters[instr.rs] & 0x1f);
			Machine.mRegisters[instr.rd] = tmp;
			break;

		case OP_SUB:
			diff = Machine.mRegisters[instr.rs] - Machine.mRegisters[instr.rt];
			if ((((Machine.mRegisters[instr.rs] ^ Machine.mRegisters[instr.rt]) & SIGN_BIT) != 0)
					&& (((Machine.mRegisters[instr.rs] ^ diff) & SIGN_BIT) != 0)) {
				Machine.raiseException(ExceptionType.OverflowException, 0);
				return;
			}
			Machine.mRegisters[instr.rd] = diff;
			break;

		case OP_SUBU:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rs] - Machine.mRegisters[instr.rt];
			break;

		case OP_SW:
			// long l = (long)(Machine.mRegisters[instr.rs] + instr.extra);

			// System.out.println(Machine.mRegisters[instr.rs] + "\t" +
			// instr.extra + "\t" + (Machine.mRegisters[instr.rs] +
			// instr.extra));
			// System.out.println(Machine.mRegisters[instr.rt]);
			if (!Machine.writeMem((Machine.mRegisters[instr.rs] + instr.extra), 4, Machine.mRegisters[instr.rt])) {
				return;
			}
			break;

		case OP_SWL:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;
			// The little endian/big endian swap code would
			// fail (I think) if the other cases are ever exercised.
			assert ((tmp & 0x3) == 0);

			result = (Integer) Machine.readMem((tmp & ~0x3), 4);
			if (result == null) {
				return;
			}
			value = result.intValue();
			switch (tmp & 0x3) {
			case 0:
				value = Machine.mRegisters[instr.rt];
				break;

			case 1:
				value = (value & 0xff000000) | ((Machine.mRegisters[instr.rt] >> 8) & 0xffffff);
				break;

			case 2:
				value = (value & 0xffff0000) | ((Machine.mRegisters[instr.rt] >> 16) & 0xffff);
				break;

			case 3:
				value = (value & 0xffffff00) | ((Machine.mRegisters[instr.rt] >> 24) & 0xff);
				break;
			}

			if (!Machine.writeMem((tmp & ~0x3), 4, value)) {
				return;
			}
			break;

		case OP_SWR:
			tmp = Machine.mRegisters[instr.rs] + instr.extra;

			// The little endian/big endian swap code would
			// fail (I think) if the other cases are ever exercised.
			assert ((tmp & 0x3) == 0);
			result = Machine.readMem((tmp & ~0x3), 4);
			if (result == null) {
				return;
			}
			value = result.intValue();
			switch (tmp & 0x3) {
			case 0:
				value = (value & 0xffffff) | (Machine.mRegisters[instr.rt] << 24);
				break;

			case 1:
				value = (value & 0xffff) | (Machine.mRegisters[instr.rt] << 16);
				break;

			case 2:
				value = (value & 0xff) | (Machine.mRegisters[instr.rt] << 8);
				break;

			case 3:
				value = Machine.mRegisters[instr.rt];
				break;
			}

			if (!Machine.writeMem((tmp & ~0x3), 4, value)) {
				return;
			}
			break;

		case OP_SYSCALL:
			Machine.raiseException(ExceptionType.SyscallException, 0);
			return;

		case OP_XOR:
			Machine.mRegisters[instr.rd] = Machine.mRegisters[instr.rs] ^ Machine.mRegisters[instr.rt];
			break;

		case OP_XORI:
			Machine.mRegisters[instr.rt] = Machine.mRegisters[instr.rs] ^ (instr.extra & 0xffff);
			break;

		case OP_RES:
		case OP_UNIMP:
			Machine.raiseException(ExceptionType.IllegalInstrException, 0);
			return;

		default:
			assert (false);
		}

		// Now we have successfully executed the instruction.

		// Do any delayed load operation
		Machine.delayedLoad(nextLoadReg, nextLoadValue);

		// Advance program counters.
		Machine.mRegisters[Machine.PrevPCReg] = Machine.mRegisters[Machine.PCReg]; // for
																					// debugging,
																					// in
																					// case
																					// we
		// are jumping into lala-land
		Machine.mRegisters[Machine.PCReg] = Machine.mRegisters[Machine.NextPCReg];
		Machine.mRegisters[Machine.NextPCReg] = pcAfter;
	}

	/**
	 * Simulate R2000 multiplication. The words at *hiPtr and *loPtr are
	 * overwritten with the double-length result of the multiplication.
	 */

	public static void Mult(int a, int b, boolean signedArith, int[] pPtrs) // int
																			// hiPtr,
																			// int
																			// loPtr)
	{
		if ((a == 0) || (b == 0)) {
			pPtrs[1] = pPtrs[0] = 0;
			return;
		}

		// Compute the sign of the result, then make everything positive
		// so unsigned computation can be done in the main loop.
		boolean negative = false;
		if (signedArith) {
			if (a < 0) {
				negative = !negative;
				a = -a;
			}
			if (b < 0) {
				negative = !negative;
				b = -b;
			}
		}

		// Compute the result in unsigned arithmetic (check a's bits one at
		// a time, and add in a shifted value of b).
		int bLo = b;
		int bHi = 0;
		int lo = 0;
		int hi = 0;
		for (int i = 0; i < 32; i++) {
			if ((a & 1) != 0) {
				lo += bLo;
				if (lo < bLo) // Carry out of the low bits?
				{
					hi += 1;
				}

				hi += bHi;
				if ((a & 0xfffffffe) == 0) {
					break;
				}
			}

			bHi <<= 1;
			if ((bLo & 0x80000000) != 0) {
				bHi |= 1;
			}

			bLo <<= 1;
			a >>= 1;
		}

		// If the result is supposed to be negative, compute the two's
		// complement of the double-word result.
		if (negative) {
			hi = ~hi;
			lo = ~lo;
			lo++;
			if (lo == 0) {
				hi++;
			}
		}

		pPtrs[1] = (int) hi;
		pPtrs[0] = (int) lo;
	}

	// Routines for converting Words and Short Words to and from the
	// simulated machine's format of little endian. These end up
	// being NOPs when the host machine is also little endian (DEC and Intel).

	public static int wordToHost(int word) {
		int result;
		result = (word >> 24) & 0x000000ff;
		result |= (word >> 8) & 0x0000ff00;
		result |= (word << 8) & 0x00ff0000;
		result |= (word << 24) & 0xff000000;
		return result;
	}

	public static int shortToHost(int shortword) {
		int result;
		result = (shortword << 8) & 0xff00;
		result |= (shortword >> 8) & 0x00ff;
		return result;
	}

	public static int wordToMachine(int word) {
		return wordToHost(word);
	}

	public static int shortToMachine(int shortword) {
		return shortToHost(shortword);
	}
}
