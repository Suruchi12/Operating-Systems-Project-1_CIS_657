#include "syscall.h"

char matFile[] = "test/matmult";
char sortFile[] = "test/sort";
int
main()
{
  int x = Fork();
  int retValue = Join(x);
  if(x == 0)
    {
      Exec(matFile);
    }
  Exec(sortFile);
  //This should not be reached
  return 0;
}
