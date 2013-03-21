#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define BUFFSIZE 1024
#define XLBUFFSIZE 2560

int main(void){
	int i = 0;
	int file, closeFile, unlinkFile, writeFile, readFile, openFile;
	char writeBuffer[BUFFSIZE], readBuffer[BUFFSIZE], XLWriteBuffer[XLBUFFSIZE], XLReadBuffer[XLBUFFSIZE];

	/* [TEST SET ONE]
	 * tests for the following syscalls: creat, close, unlink
	 */
	for(i =0; i<15; i++){
		// testing creat syscall; will attempt to create the test file
		file = creat("testFile.txt");
		if(file == -1){
			printf("[ERROR] creat failed for testFile.txt on %d-th attempt", i);
			return 1;
		}

		// testing close syscall; will attempt to close the test file
		closeFile = close(file);
		if(closeFile == -1){
			printf("[ERROR] close failed for testFile.txt on %d-th attempt", i);
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
			printf("[ERROR] read: %c vs wrote %c", readBuffer[i], writeBuffer[i]);
			return 1;
		}
	}

	/* [TEST SET THREE]
	 * testing misc edge cases
	 */
	// testing unlink while a file is still being accessed (not yet closed)
	// this should NOT delete the file and we should still be able to read it
	unlinkFile = unlink("testFile2.txt");
	readFile = read(file, readBuffer, 64);
	if(unlinkFile == -1 || readFile == -1){
		printf("[ERROR] unlink failed or deleted a file before it was closed");
		return 1;
	}

	// testing open while a file has already been closed
	// we should NOT be able to open the file anymore and read it
	closeFile = close(file);
	openFile = open("testFile2.txt");
	if(closeFile == -1 || openFile != -1){
		printf("[ERROR] close failed or was able to open a file after it was closed");
		return 1;
	}

	/* [TEST SET FOUR]
	 * testing large reads and writes
	 */
	// testing large write
	for(i=0; i<XLBUFFSIZE; i++) {
		XLWriteBuffer[i] = '.';
	}
	file = creat("XLTestFile.txt");
	if(file == -1){
		printf("[ERROR] creat failed on XLTestFile.txt");
		return 1;
	}
	writeFile = write(file, XLWriteBuffer, XLBUFFSIZE);
	if(writeFile == -1){
		printf("[ERROR] write failed for XLTestFile.txt");
		return 1;
	}
	close(file);

	// testing large read
	file = open("XLTestFile.txt");
	if(file == -1){
	  printf("[ERROR] open failed for XLTestFile.txt");
	  return 1;
	}
	readFile = read(file, XLReadBuffer, XLBUFFSIZE);
	if(readFile == -1){
		printf("[ERROR] read failed for XLTestFile.txt");
		return 1;
	}

	// comparing contents
	for(i=0; i<XLBUFFSIZE; i++){
		if (XLWriteBuffer[i] != XLReadBuffer[i]) {
			printf("[ERROR] content written to XLTestFile.txt does not match content read from XLTestFile.txt; failed at %d-th byte", i);
			return 1;
		}
	}
	unlink("XLTestFile.txt");
	close(file);

    return 1;
}
