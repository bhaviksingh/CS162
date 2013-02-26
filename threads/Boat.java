package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;
	static int numChildrenOnOahu;
	static int numAdultsOnOahu;
	static int numChildrenOnMolokai;
	static int numAdultsOnMolokai;
	//static boolean isBoatOnOahu;
	static boolean isDone;
	static int boatLocation; // 0 for Oahu, 1 for Molokai
	//static int numPassangers;
	static Lock pilotLock;
	static Lock gameEnd;
	static Condition readyAtOahu;
	static Condition readyAtMolokai;
	//static Condition passangerChildNeeded;
	static Condition end;
	static Communicator communicator;	
	//static boolean hasChildPilot;

	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		//System.out.println("\n ***Testing Boats with only 2 children***");
		//begin(0, 2, b);

		System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		begin(1, 2, b);

		//System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//begin(3, 3, b);
	}

	public static void begin(int adults, int children, BoatGrader b) {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here.
		isDone = false;
		boatLocation = 0;
		numChildrenOnOahu = children;
		numChildrenOnMolokai = 0;
		numAdultsOnOahu = adults;
		numAdultsOnMolokai = 0;
		//numPassangers = 0;
		//hasChildPilot = false;
		//boolean gameOver = false;
		pilotLock = new Lock();
		readyAtOahu = new Condition(pilotLock);
		readyAtMolokai = new Condition(pilotLock);
		//passangerChildNeeded = new Condition(pilotLock);
		gameEnd = new Lock();
		end = new Condition(gameEnd);
		
		Runnable childRunnable = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};
		
		Runnable adultRunnable = new Runnable() {
			public void run() {
				AdultItinerary();
			}
		};

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.
		for (int i = 0; i < children; i++) {
			KThread child = new KThread(childRunnable);
			child.setName("Child");
			System.out.println("--- created "+i+" child threads");
			child.fork();			
		}

		for (int i = 0; i < adults; i++) {
			KThread adult = new KThread(adultRunnable);
			adult.setName("Adult");
			System.out.println("--- created "+i+" adult threads");
			adult.fork();			
		}
		
		System.out.println("--- reached checkpoint 1");

		while (true) {			
			//end.sleep();
			if (numChildrenOnOahu + numAdultsOnOahu == 0){
				System.out.println("Game over. FUCK YEAH!");
				//isDone = true;				
				//end.wake(); // do we need to do anything with end or gameEnd here?				
				break;
			}	
			else {
				System.out.println("Game is not over yet.");
				pilotLock.acquire();
				readyAtMolokai.wake();
				pilotLock.release();
				gameEnd.acquire();
				end.sleep();			
			}
			//System.out.println("--- not done yet");
		}
		
		System.out.println("--- reached checkpoint 2");

	}

	static void printState() {
		System.out.println("Oahu:[" + numAdultsOnOahu + ","
				+ numChildrenOnOahu + "]" + " Molokai:[" + numAdultsOnMolokai + ","
				+ numChildrenOnMolokai + "]");
	}

	static void AdultItinerary()
	{
		System.out.println("--- called adult itinerary");
		/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
		 */
		pilotLock.acquire();
<<<<<<< HEAD
		while (true) {
			System.out.println("______In AdultItinerary______");
			if (boatLocation == 1) { // if the boat is at Molokai, no adult can cross to Molokai
				System.out.println("______Boat is at MOLOKAI______");
				return;
			}
			if (boatLocation !=  currentLocation){ //if boat is not at the current location
				readyAtOahu.sleep();
			}
			if (numChildrenOnOahu >= 2) { //if Oahu has >= 2 children, donÕt send adults (you will need to send children)
=======
		
		int currentLocation = 0;
		
		while(true) {
			if(boatLocation != currentLocation || numChildrenOnOahu >= 2) {
>>>>>>> 51ed4a3036a78156fe780a6bf13c738f35288124
				readyAtOahu.sleep();
			}		
			else {		
				numAdultsOnOahu--;
				bg.AdultRowToMolokai();
				numAdultsOnMolokai++;
				boatLocation = 1;
				currentLocation = 1;		
				readyAtMolokai.wake();
				break;
			}
		}
					
		//pilotLock.release(); 		
	}

	static void ChildItinerary() {
		System.out.println("--- called child itinerary");
		Boolean readyToTerminate = false;
		pilotLock.acquire();
		int currentLocation = 0; //current location set to Oahu
		//numChildrenOnOahu++;
		printState();
		//pilotLock.release(); 

		//pilotLock.acquire();
		while (true) {
			System.out.println("______In ChildItinerary______");
			if (currentLocation == 0) { //in Oahu
<<<<<<< HEAD
				if (boatLocation == 0){
					if (numChildrenOnOahu == 1 && !hasChildPilot) {
						//readyAtOahu.wake();
						//hasChildPilot = true;
						readyAtOahu.sleep();
=======
				//System.out.println("--- checkpoint 1; we are in Oahu");
				if (boatLocation != 0 || numChildrenOnOahu == 1){											
					readyAtOahu.sleep();
				}
				else { 
					if(numAdultsOnOahu == 0 && numChildrenOnOahu == 2) {
						readyToTerminate = true;
>>>>>>> 51ed4a3036a78156fe780a6bf13c738f35288124
					}
					//2 kids to oahu
					bg.ChildRowToMolokai();
					numChildrenOnOahu-=2;
					//numChildrenOnOahu--;					
					numChildrenOnMolokai+=2;
					//numChildrenOnMolokai++;
					currentLocation = 1;
					boatLocation = 1;
					
					printState();
					if (readyToTerminate) {						
						gameEnd.acquire();
						end.wake();
						gameEnd.release();
						readyAtMolokai.sleep();
					}
					else {
						readyAtMolokai.wake();
					}
				}
<<<<<<< HEAD
			else { //if current location is at Molokai, send a child as the pilot
					if (boatLocation == 1) {
						boatLocation = 0;
						currentLocation = 0;
						numChildrenOnMolokai--;
						bg.ChildRowToOahu();
						numChildrenOnOahu++;
						readyAtOahu.wake();
						readyAtOahu.sleep();
					} 
					else {
						readyAtMolokai.sleep();
					}
=======
			}			
			else {
				if(boatLocation != 1) {
					readyAtMolokai.sleep();
				}
				else {
					// send a child back to Oahu
					boatLocation = 0;
					currentLocation = 0;
					numChildrenOnMolokai--;
					bg.ChildRowToOahu();
					numChildrenOnOahu++;
					readyAtOahu.wake();
>>>>>>> 51ed4a3036a78156fe780a6bf13c738f35288124
				}
			}
		}		
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out
		.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}
