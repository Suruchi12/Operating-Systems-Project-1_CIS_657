package jnachos.kern;

import jnachos.machine.Machine;

public class childproc implements VoidFunctionPtr
{
    @Override
    public void call(Object pArg)
    {

        try
        {
        System.out.println(" Fork-Create a new NachosProcess (the child).");

        JNachos.getCurrentProcess().restoreUserState();
        JNachos.getCurrentProcess().getSpace().restoreState();


        System.out.println("Child Process is invoked now ");
        Machine.run();

        assert(false);
    }
    catch(Exception e)
    {
    System.out.println(e);
}

}
}
