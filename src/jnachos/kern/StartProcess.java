/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

/**
 *
 * @author pjmcswee
 */
public class StartProcess implements VoidFunctionPtr {

	/**
	 * Starts a user process written in C. Run a user program. Open the
	 * executable, load it into memory, and jump to it.
	 **/
	public void call(Object arg) {

		String filename = (String) arg;

		// The executable file to run
		OpenFile executable = JNachos.mFileSystem.open(filename);

		// If the file does not exist
		if (executable == null) {
			Debug.print('t', "Unable to open file" + filename);
			return;
		}

		// Load the file into the memory space
		AddrSpace space = new AddrSpace(executable);
		JNachos.getCurrentProcess().setSpace(space);

		// set the initial register values
		space.initRegisters();

		// load page table register
		space.restoreState();

		// jump to the user progam
		// machine->Run never returns;
		Machine.run();

		// the address space exits
		// by doing the syscall "exit"
		assert (false);
	}
}
