# Copyright (c) 1992 The Regents of the University of California.
# All rights reserved.  See copyright.h for copyright notice and limitation 
# of liability and disclaimer of warranty provisions.

MAKE = gmake

# comment out the last two lines of make all, if we are on any machine
# other than a MIPS -- we need the cross-compiler for the programs in "test"
all: 
	cd threads; $(MAKE) depend
	cd threads; $(MAKE) nachos
	cd userprog; $(MAKE) depend 
	cd userprog; $(MAKE) nachos 
	cd vm; $(MAKE) depend
	cd vm; $(MAKE) nachos 
	cd filesys; $(MAKE) depend
	cd filesys; $(MAKE) nachos 
	cd network; $(MAKE) depend
	cd network; $(MAKE) nachos 
	cd test; make all
	cd bin; make all

# but don't delete executables in test in case there is no cross-compiler
clean:
	/bin/csh -c "rm -f */{core,nachos,DISK,*.o,swtch.s} test/{*.coff} bin/{coff2flat,coff2noff,disassemble}"

print:
	/bin/csh -c "enscript -2r Makefile* */Makefile"
	/bin/csh -c "enscript -2r threads/*.h threads/*.cc threads/*.s"
	/bin/csh -c "enscript -2r userprog/*.h userprog/*.cc" 
	/bin/csh -c "enscript -2r filesys/*.h filesys/*.cc
	/bin/csh -c "enscript -2r network/*.h network/*.cc 
	/bin/csh -c "enscript -2r machine/*.h machine/*.cc
	/bin/csh -c "enscript -2r bin/noff.h bin/coff.h bin/coff2noff.c"
	/bin/csh -c "enscript -2r test/*.h test/*.c test/*.s"
