#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFSIZE 1024
#define XLPAGESIZE 2560

char buf1[PAGESIZE], buf2[PAGESIZE], buf3[XLPAGESIZE], buf4[XLPAGESIZE];

int main(void){
	int i = 0;
	int file;

	//int writeFile, readFile, openFile;
	char fileName[1000];

	/* [TEST SET ONE]
	 * tests for the following syscalls: creat, close, unlink
	 */
	int closeFile, unlinkFile;
	for(i =0; i<20; i++){
		// testing creat syscall; will attempt to create the test file 20 times
		file = creat("testFile.txt");
		if(file == -1){
			printf("[ERROR] creat failed for testFile.txt on %d-th attempt", i);
			return 1;
		}

		// testing close syscall; will attempt to close the test file 20 times
		closeFile = close(file);
		if(closeFile == -1){
			printf("[ERROR] close failed for testFile.txt on %d-th attempt", i);
			return 1;
		}

		// testing unlink syscall; will attempt to delete the test file 20 times
		unlinkFile = unlink(file);
		if(unlinkFile == -1) {
			printf("[ERROR] unlink failed for testFile.txt on %d-th attempt", i);
		}
	}

	/* [TEST SET ONE]
	 * tests for the following syscalls: read, write
	 */
	char writeBuffer[BUFFSIZE];
	char readBuffer[BUFFSIZE];
	int writeFile, readFile;
	file = creat("testFile2.txt");
	if(file == -1){
		printf("[ERROR] Create failed on test2.txt");
		return 1;
	}

	// testing write syscall; will attempt to write 64 bytes to the newly created test file then close it
	for(i=0; i<64; i++){
		writeBuffer[i] = ".";
	}
	writeFile = write(file, writeBuffer, 64);
	if(writeFile == -1){
		printf("[ERROR] write failed for testFile2.txt");
		return 1;
	}
	else {
		printf("[INFO] write succeeded for testFile2.txt; wrote %d bytes", writeFile);
	}
	close(file);

	// testing read syscall; will attempt to open the newly created and written test file then read it
	file = open("testFile2.txt");
	readFile = read(file, readBuffer, 64);
	if(readFile == -1){
		printf("[ERROR] read failed for testFile2.txt");
		return 1;
	}
	else {
		printf("[INFO] read succeeded for testFile2.txt; read %d bytes", readFile);
	}

	// double check that what we wrote = what we read
	for(i=0; i<64; i++){
		if (readBuffer[i] != writeBuffer[i]){
			printf("[ERROR] content written to testFile2.txt does not match content read from testFile2.txt; failed at %d-th byte", i);
			return 1;
		}
	}

	//Unlink while a source is accessing it (Pending for deletion)
	unlinkDaFile = unlink("test2.txt");
	if(unlinkDaFile == -1){
		printf("ERROR: Couldn't unlink test2.txt");
		return 1;
	}
	//Shouldn't be deleted because not yet closed
	readDaFile = read(daFile,buf2,64);
	if(readDaFile == -1){
		printf("ERROR: Unlink deleted a file with a source still accessing it");
		return 1;
	}
	//Close the source (Should be deleted after this)
	closeDaFile = close(daFile);
	if(closeDaFile == -1){
		printf("ERROR: Close failed as last one referencing it");
		return 1;
	}
	//Should fail to open
	openDaFile = open("test2.txt");//this should not create the file
	if(openDaFile != -1){
		printf("ERROR: File should not exist anymore");
		return 1;
	}


	//Tests for large size Read & Write
	for(i=0; i<bigsize; i++)
		buf3[i] = 'x' + i;
	daFile = creat("bigTest.txt");
	if(daFile == -1){
		printf("ERROR: Couldn't create file because too big");
		return 1;
	}
	writeDaFile = write(daFile, buf3, bigsize);
	if(writeDaFile == -1){
		printf("ERROR: Couldn't write big data");
		return 1;
	}
	close(daFile);
	daFile = open("bigTest.txt");
	if(daFile == -1){
	  printf("ERROR: Couldn't open file because too big");
	  return 1;
	}
	readDaFile = read(daFile, buf4, bigsize);
	if(readDaFile == -1){
		printf("ERROR: Couldn't read because too big");
		return 1;
	}
	for(i=0; i<bigsize; i++){
		if (buf3[i] != buf4[i]) {
			printf("ERROR: Wrong data read back for bigTest");
			return 1;
		}
	}
	unlink("bigTest.txt");
	close(daFile);


	//Tests with bad input
	//Checks for larger than 256 byte file names
	for(i=0; i<1000; i++){
		fileName[i]=i;
	}
	daFile = creat(fileName);
	if(daFile != -1){
		printf("ERROR: Filename exceeded 256");
		return 1;
	}
	//More than 16 concurrent files
	daFile = creat("fillEmUpTest.txt");
	close(daFile);
	for(i=0; i<15; i++){
		openDaFile = open("fillEmUpTest.txt");
		if(openDaFile == -1 && i <14){
			printf("ERROR: Too many files in the FD: %d", i);
			return 1;
		}
	}
	for(i=2; i<16; i++){
		close(i);
	}


	//Tests to close STDIN & STDOUT
	closeDaFile = close(0);
	if(closeDaFile == -1){
		printf("ERROR: Couldn't close stdin");
		return 1;
	}
	printf("Success! You are awesome Lewis");
	closeDaFile = close(1);
	if(closeDaFile == -1){
		printf("ERROR: Couldn't close stdout");
		return 1;
	}


    return 1;
}
