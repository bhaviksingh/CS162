#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(void) {
/*Creates n processes, runs them until they are finished, and repeats. Checks to make sure that memory is being
* properly deallocated at the end of a process.
*/
    int n = 15;
    int childPID[n];
    int *exitStatus;
    char *cpargv[10] = {"TESTING"};
    for(int i = 0; i<n; i++){
	childPID[i] = exec("test.coff", 1, cpargv);
	join(childPID[i], exitStatus);
	printf("child pid: %d \n", childPID[i]);
    }
    printf("FINISHED!");
}
