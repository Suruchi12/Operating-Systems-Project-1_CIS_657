/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import jnachos.machine.*;
import jnachos.filesystem.*;

/** The class handles System calls made from user programs. */
public class SystemCallHandler {
	/** The System call index for halting. */
	public static final int SC_Halt = 0;

	/** The System call index for exiting a program. */
	public static final int SC_Exit = 1;

	/** The System call index for executing program. */
	public static final int SC_Exec = 2;

	/** The System call index for joining with a process. */
	public static final int SC_Join = 3;

	/** The System call index for creating a file. */
	public static final int SC_Create = 4;

	/** The System call index for opening a file. */
	public static final int SC_Open = 5;

	/** The System call index for reading a file. */
	public static final int SC_Read = 6;

	/** The System call index for writting a file. */
	public static final int SC_Write = 7;

	/** The System call index for closing a file. */
	public static final int SC_Close = 8;

	/** The System call index for forking a forking a new process. */
	public static final int SC_Fork = 9;

	/** The System call index for yielding a program. */
	public static final int SC_Yield = 10;

	/**
	 * Entry point into the Nachos kernel. Called when a user program is
	 * executing, and either does a syscall, or generates an addressing or
	 * arithmetic exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll
	 * loop making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	/**
	 * @param pWhichSysCall
	 */
	public static void handleSystemCall(int pWhichSysCall) {
		
        try
        {
            System.out.println("SysCall:" + pWhichSysCall);
            System.out.println("Process ID: " + JNachos.getCurrentProcess().getpid());
          //  Scheduler obj=new Scheduler();
            //increment the program counter
            Machine.writeRegister(Machine.PCReg, Machine.mRegisters[Machine.NextPCReg]);
            Machine.writeRegister(Machine.NextPCReg, Machine.mRegisters[Machine.NextPCReg] + 4);

            switch (pWhichSysCall) {
                // If halt is received shut down

                case SC_Halt:
                    Debug.print('a', "Shutdown, initiated by user program.");
                    Interrupt.halt();
                    break;


                case SC_Fork:

                    //to fork
                    childfork();


                    break;


                case SC_Exit:
                    // Read in any arguments from the 4th register
                    int arg = Machine.readRegister(4);
                    System.out.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);
                    //check for the waiting process
                    //Join- req 4-6
                    Scheduler.checkwaitingproc(JNachos.getCurrentProcess().getpid(), arg);
                    // Finish the invoking process
                    JNachos.getCurrentProcess().finish();

                    break;


                case SC_Join:

                    boolean interuptforjoin = Interrupt.setLevel(false);
                    System.out.println("JOIN SYSTEM CALL IS INVOKED BY PROCESS " + JNachos.getCurrentProcess().getpid());

                        //checks if the process exists
                    if (!Scheduler.checkProcess(Machine.readRegister(4)) || Machine.readRegister(4) == JNachos.getCurrentProcess().getpid()||Machine.readRegister(4)==0 ) {
//
                        break;
                    }
                    else
                        {
                        System.out.println("PROCESS  "+JNachos.getCurrentProcess().getpid() + " will be put to sleep and will wait for the process :-"+ Machine.readRegister(4) +" to finish execution ");
                        //this will put the current process in the waiting table -Req 2 Join
                            Scheduler.waitingprocessTable(Machine.readRegister(4), JNachos.getCurrentProcess());
                        //Join req 3 Put the invoking process to sleep

                        JNachos.getCurrentProcess().sleep();
                    }

                    Interrupt.setLevel(interuptforjoin);

                    Machine.run();

                    break;

                case SC_Exec:
                    //call exec function
                    execfunc();


                    break;


                default:
                    Interrupt.halt();
                    break;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        }
//exec system call
    private static void execfunc() {

        String file = new String();
        boolean interuptexec = Interrupt.setLevel(false);

        System.out.println("EXEC SYSTEM CALL IS BEING CALLED BY PROCESS "+JNachos.getCurrentProcess().getpid() );

        //extract the filename character by character
         file=  extractfilename();


        //Exec Req 2 Open the file and overwrite the contents of this processs address space with its content.
        //Exec req 3 Reset the processs registers to their initial default  state.


        OpenFile executablefile;
        executablefile = JNachos.mFileSystem.open(file);
        if(executablefile==null)
        {
            System.out.println("File path is not correct");
        }
        System.out.println("The file being executed is " + file);
        AddrSpace newaddressspace;
        newaddressspace = new AddrSpace(executablefile);
        JNachos.getCurrentProcess().setSpace(newaddressspace);
        JNachos.getCurrentProcess().getSpace().initRegisters();
        JNachos.getCurrentProcess().getSpace().restoreState();

        Interrupt.setLevel(interuptexec);

        Machine.run();


    }
    //Exec Req 1 Get the String parameter from the processs address space (only a pointer passed in).
    //Obtain the userprogram
    private static String extractfilename()
    {

                String file=new String();
        int regvalue = Machine.readRegister(4);
        int asciivalue = 1;
        while((char) asciivalue!='\0')
        {
            asciivalue = Machine.readMem(regvalue, 1);
            if((char) asciivalue!= '\0')
            {
                file = file + (char) asciivalue;
            }

            regvalue++;
        }
       return file;
    }

    private static void childfork()
    {
		System.out.println("FORK SYSTEM CALL  IS INVOKED BY  PROCESS : "+ JNachos.getCurrentProcess().getpid());

		boolean forkinterupt = Interrupt.setLevel(false);

		Machine.writeRegister(2, 0);


        // Fork-Req -1 create a new NachosProcess (the child).
        NachosProcess childprocess = new NachosProcess(JNachos.getCurrentProcess().getName() + "child");

		//copy the address space of the parent and give the child the same address space
        /* Fork-Req-2 Copy the memory space of the parent to a new
        section of RAM for the child, in the process setting
        up the childs memory map. */

		childprocess.setSpace(new AddrSpace(JNachos.getCurrentProcess().getSpace()));
		//Fork-Req-3save the child process state and Copy the parents registers into the childs set of registers
      //  with the notable exception that the return
        //    value of the Fork system call should be different
		childprocess.saveUserState();

		Machine.writeRegister(2, childprocess.getpid());
		//Fork-Req-4Call the NachosProcess::fork member function make the child Ready.
		childprocess.fork(new childproc(), childprocess);

		Interrupt.setLevel(forkinterupt);

	}
}
