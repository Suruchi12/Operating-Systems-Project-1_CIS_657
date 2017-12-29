# Use normal make for this Makefile
#
# Makefile for:
#	coff2noff -- converts a normal MIPS executable into a Nachos executable
#	disassemble -- disassembles a normal MIPS executable 
#
# Copyright (c) 1992 The Regents of the University of California.
# All rights reserved.  See copyright.h for copyright notice and limitation 
# of liability and disclaimer of warranty provisions.

CFLAGS= -O0 -g2 -I./ -I../threads

# If the host is big endian (SPARC, SNAKE, etc):
# 	disassemble and coff2flat won't work
#	coff2flat must be compiled with a special flag
# CFLAGS= -O0 -g2 -I./ -I../threads -DHOST_IS_BIG_ENDIAN

all: coff2noff disassemble coff2flat

# converts a COFF file to Nachos object format
coff2noff: coff2noff.o
	cc -g2 -O0 coff2noff.o -o coff2noff

# converts a COFF file to a flat address space (for Nachos version 2)
coff2flat: coff2flat.o
	cc -g2 -O0 coff2flat.o -o coff2flat

# dis-assembles a COFF file
disassemble: out.o opstrings.o
	cc -g2 -O0 out.o opstrings.o -o disassemble
