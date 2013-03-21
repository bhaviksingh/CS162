#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

#define size 1024
#define bigsize 2560

char buf1[size], buf2[size], buf3[bigsize], buf4[bigsize];

int main(void){
	//Tests for any modifications to the FD Array (Creat, Unlink & Close)
	int i, daFile, writeDaFile, readDaFile, openDaFile, closeDaFile, unlinkDaFile;
	char fileName[1000];


	for(i =0; i<17; i++){
		daFile = creat("test1.txt");
		if(daFile == -1){
			printf("ERROR: Creat failed on FD %d", i);
			return 1;
		}
		closeDaFile = close(daFile);
		if(closeDaFile == -1){
			printf("ERROR: Close failed on FD %d", i);
			return 1;
		}
		unlink("test.txt");
	}


	//Tests for Read & Write
	daFile = creat("test2.txt");
	if(daFile == -1){
		printf("ERROR: Creat failed on test2.txt");
		return 1;
	}
	for(i=0; i<64; i++){
		buf1[i] = 'x' + i;
	}
	writeDaFile = write(daFile, buf1, 64);
	if(writeDaFile == -1){
		printf("ERROR: Couldn't write to test2.txt");
		return 1;
	}
	close(daFile);
	daFile = open("test2.txt");
	readDaFile = read(daFile, buf2, 64);
	if(readDaFile == -1){
		printf("ERROR: Couldn't read test2.txt");
		return 1;
	}
	for(i=0; i<64; i++){
		if (buf2[i] != buf1[i]){
			printf("ERROR: Wrong contents in test2.txt");
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