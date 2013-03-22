#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFSIZE 64
#define XLBUFFSIZE 2560

char writeBuffer[BUFFSIZE], readBuffer[BUFFSIZE], XLWriteBuffer[XLBUFFSIZE], XLReadBuffer[XLBUFFSIZE];

int main(void){
	int i = 0;
	int file, XLFile, closeFile, unlinkFile, writeFile, readFile, openFile, toWrite;

	/* [TEST SET ONE]
	 * tests for the following syscalls: creat, close, unlink
	 */
	for(i =0; i<15; i++){
		// testing creat syscall; will attempt to create the test file
		file = creat("testFile.txt");
		if(file == -1){
			printf("[ERROR] creat failed for testFile.txt on %d-th attempt\n", i);
			return 1;
		}

		// testing close syscall; will attempt to close the test file
		closeFile = close(file);
		if(closeFile == -1){
			printf("[ERROR] close failed for testFile.txt on %d-th attempt\n", i);
			return 1;
		}

		// testing unlink syscall; will attempt to delete the test file
		unlink(file);
	}

	/* [TEST SET TWO]
	 * tests for the following syscalls: read, write
	 */
	file = creat("testFile2.txt");
	if(file == -1){
		printf("[ERROR] Create failed on test2.txt\n");
		return 1;
	}

	// testing write syscall; will attempt to write to the newly created test file then close it
	for(i=0; i<BUFFSIZE; i++){
		//toWrite = i % 64;
		//printf("toWrite = %d", toWrite);
		writeBuffer[i] = i + '0';
	}
	writeFile = write(file, writeBuffer, BUFFSIZE);
	if(writeFile == -1){
		printf("[ERROR] write failed for testFile2.txt\n");
		return 1;
	}
	else {
		printf("[INFO] write succeeded for testFile2.txt; wrote %d bytes\n", writeFile);
		printf("[INFO] writeBuffer: %s\n", writeBuffer);
	}
	close(file);

	// testing read syscall; will attempt to open the newly created and written test file then read it
	file = open("testFile2.txt");
	readFile = read(file, readBuffer, BUFFSIZE);
	if(readFile == -1){
		printf("[ERROR] read failed for testFile2.txt\n");
		return 1;
	}
	else {
		printf("[INFO] read succeeded for testFile2.txt; read %d bytes\n", readFile);
		printf("[INFO] readBuffer: %s\n", readBuffer);
	}

	// double check that what we wrote = what we read
	for(i=0; i<BUFFSIZE; i++){
		if (readBuffer[i] != writeBuffer[i]){
			printf("[ERROR] content written to testFile2.txt does not match content read from testFile2.txt; failed at %d-th byte\n", i);
			printf("[ERROR] read: %s vs wrote: %s\n", readBuffer[i], writeBuffer[i]);
			return 1;
		}
	}

	/* [TEST SET THREE]
	 * testing misc edge cases
	 */
	// testing unlink while a file is still being accessed (not yet closed)
	// this should NOT delete the file and we should still be able to read it
	unlinkFile = unlink("testFile2.txt");
	readFile = read(file, readBuffer, BUFFSIZE);
	if(unlinkFile == -1 || readFile == -1){
		printf("[ERROR] unlink failed or deleted a file before it was closed\n");
		return 1;
	}

	// testing open while a file has already been closed
	// we should NOT be able to open the file anymore and read it
	closeFile = close(file);
	openFile = open("testFile2.txt");
	if(closeFile == -1 || openFile != -1){
		printf("[ERROR] close failed or was able to open a file after it was closed\n");
		return 1;
	}
	unlink("testFile2.txt");

	/* [TEST SET FOUR]
	 * testing large reads and writes
	 */
	// testing large write
	XLFile = creat("XLTestFile.txt");
	if(XLFile == -1){
		printf("[ERROR] creat failed on XLTestFile.txt\n");
		return 1;
	}

	for(i=0; i<XLBUFFSIZE; i++) {
		XLWriteBuffer[i] = i + '0';
	}
	printf("[INFO] preparing to write to XLTestFile.txt: %s\n", XLWriteBuffer);
	writeFile = write(XLFile, XLWriteBuffer, XLBUFFSIZE);
	if(writeFile == -1){
		printf("[ERROR] write failed for XLTestFile.txt\n");
		printf("current state of buffer: %s\n", XLWriteBuffer);
		return 1;
	}
	else {
		printf("[INFO] write succeeded for XLTestFile.txt; wrote %d bytes\n", writeFile);
	}
	close(XLFile);

	// testing large read
	XLFile = open("XLTestFile.txt");
	if(XLFile == -1){
	  printf("[ERROR] open failed for XLTestFile.txt\n");
	  return 1;
	}

	readFile = read(XLFile, XLReadBuffer, XLBUFFSIZE);
	if(readFile == -1){
		printf("[ERROR] read failed for XLTestFile.txt\n");
		return 1;
	}
	else if(readFile == -100) {
		printf("!!! YOLOOOOO %d !!!\n", readFile);
		return 1;
	}
	else {
		printf("[INFO] read succeeded for XLTestFile.txt; read %d bytes\n", readFile);
		printf("current state of buffer: %s\n", XLReadBuffer);
	}

	// comparing contents
	for(i=0; i<XLBUFFSIZE; i++){
		if (XLWriteBuffer[i] != XLReadBuffer[i]) {
			printf("[ERROR] content written to XLTestFile.txt does not match content read from XLTestFile.txt; failed at %d-th byte", i);
			return 1;
		}
	}
	unlink("XLTestFile.txt");
	close(XLFile);

    return 1;
}
