# System Calls in JNachos

#### Operating Systems
#### CIS 657
#### Project 1

## Program Arguments:
Unzip the file JNachosLabLab2Solution into your workspace and use the option new and project from existing workspace to run in IntelliJ
for eclipse
For running this project, Create a new project from an existing source and uncheck the "use default location" box and find the directory where your project is.


## My program arguements used to run the program
IDE Used:IntelliJ.Eclipse
-x C:\Users\shara\Desktop\osnew\JNachosLab2Solution\test\fork
Output of the program can be found in output.txt

Runs for two files when separated by comma 

### Workings of the Project

Implemented Join,Fork,Exit,Exec System Calls
Fork System call 
when  the user program encounters fork system call we disable the interrupts and before creating a NachosProcess we write to the register
Once the new process is created we need to set the address space of the child by copying the memory space of the parent and save the state of the child using functions setSpace() and saveUserState.
Although, the child return 0 while the parent will return the newly created child's processs ID. The fork() method in the system call, will call another class called childproc
Then using Machine.run() we invoke the child process



Exec :
In Exec,the processs memory space is replaced with the address space of a program, located in the test directory.
We accept the file to be opened using the  address space of the process .We iterate through the values in the memory to obtain the path of the file to be executed .Once we obtain the filename
we pass it through the JavaFileSystem and check if the path is valid or not.We then create an address space to run a user program and load the program from executable.
We then set the initial values for the user-level register set and restore the machine state so that this address space can run.Later we call Machine.run()


Join
Since we have made the waiting table in Scheduler.java we can get the list of processes in the readyList in the scheduler.
In Join, after checking the process id exists, we put the waiting process in a HashMap(data structure) called processTable so it can be accessed later.

This is done by calling the function, waitingprocessTable(called in SystemCallHandler.java), declared in Scheduler.java.
This invoking process is put to sleep. 

If the process calls the exit state then we check if there's another process waiting to finish.
If there is a process is waiting to finish then we save the input to Exit to the waiting process so that it is the return value
from Join. 
In the exit system call, we call checkwaitingproc(called in SystemCallHandler.java in SC_Exit), declared in Scheduler.java. 
This checks if there is another process waiting for it to finish. If it does, then we make 
that process ready to run.


