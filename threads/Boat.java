package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	
	static BoatGrader bg;
	static int numChildrenOnOahu;
	static int numAdultsOnOahu;
	static int numChildrenOnMolokai;
	static int numAdultsOnMolokai;
	static Lock pilotLock;
	static Condition2 adultReadyAtOahu;
	static Condition2 childReadyAtOahu;
	static Condition2 childReadyAtMolokai;
	static Lock gameEnd;
	static Condition end;
	static boolean isDone;
	static Communicator communicator;	
	static int boatLocation; // 0 for Oahu, 1 for Molokai	
	static boolean hasChildPilot;

	public static void begin(int adults, int children, BoatGrader b) {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here.
		numChildrenOnOahu = children; // all children start on Oahu
		numAdultsOnOahu = adults; // all adults start on Oahu
		numChildrenOnMolokai = 0;		
		numAdultsOnMolokai = 0;
		pilotLock = new Lock(); // lock for traveling between islands
		adultReadyAtOahu = new Condition2(pilotLock);
		childReadyAtOahu = new Condition2(pilotLock);
		childReadyAtMolokai = new Condition2(pilotLock);
		gameEnd = new Lock(); // lock for whether or not to terminate
		end = new Condition(gameEnd);
		isDone = false;
		communicator = new Communicator();
		boatLocation = 0; // boat starts on Oahu	
		hasChildPilot = false;// starts with no ppl in boat
		
		gameEnd.acquire();
				
		// Logic for child threads to run.
		Runnable childRunnable = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};
		
		// Logic for adult threads to run.
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
			child.fork();			
		}

		for (int i = 0; i < adults; i++) {
			KThread adult = new KThread(adultRunnable);
			adult.setName("Adult");			
			adult.fork();			
		}

		while (!isDone) {	
			end.sleep();
			
			// If we've moved all adults and children from Oahu, then we're done!
			if (numChildrenOnOahu + numAdultsOnOahu == 0){
				System.out.println("Game over! Whoo!");	
				isDone = true;
				break;
			}				
		}			
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
		
		// While the boat is not at Oahu or there are more than 2 children on Oahu,
		// put the adult on Oahu thread to sleep (because we wouldn't send and adult in
		// these scenarios).
		while(boatLocation != 0 || numChildrenOnOahu >= 2) {
			System.out.println("--- can't send an adult to Molokai right now");
			adultReadyAtOahu.sleep();
		}
		
		System.out.println("--- sending an adult to Molokai");
		numAdultsOnOahu--; // an adult is rowing Oahu --> Molokai, so decrement
		bg.AdultRowToMolokai();		
		numAdultsOnMolokai++; // adult has rowed over, so increment
		printState();
		boatLocation = 1; // boat is now at Molokai			
		childReadyAtMolokai.wake(); // wake up child at Molokai since it's now their turn						
		
		pilotLock.release();				 
	}

	static void ChildItinerary() {
		System.out.println("--- called child itinerary");
			
		pilotLock.acquire();
		
		Boolean readyToTerminate = false;		
		int currentLocation = 0; // current location set to Oahu					
		
		while (!isDone) {						
			if (currentLocation == 0) { 
				// If we are currently on Oahu...
				while (boatLocation != 0 || numChildrenOnOahu == 1){
					System.out.println("--- child currently on Oahu, but the boat is not on Oahu ("+boatLocation+") or we only have 1 child on Oahu ("+numChildrenOnOahu+")");
					// ...but the boat is not in Oahu or we only have 1 child on Oahu, then sleep.
					childReadyAtOahu.sleep();					
				}
				
				System.out.println("--- child currently on Oahu, ready to go");
				
				if(numAdultsOnOahu == 0 && numChildrenOnOahu == 2) {						
					// ... we are ready to end the game!
					System.out.println("--- ready to end the game");
					readyToTerminate = true;
				}
				
				if(!hasChildPilot) {
					hasChildPilot = true;
					bg.ChildRowToMolokai();
					printState();
					currentLocation = 1; // this child are now now on Molokai					
				}
				else {
					numChildrenOnOahu -= 2; // 2 kids will go Oahu --> Molokai, so decrement
					bg.ChildRideToMolokai();						
					numChildrenOnMolokai += 2; // 2 kids have arrived on Molokai, so increment				
					printState();
					currentLocation = 1; // this child are now now on Molokai
					boatLocation = 1; // boat is now at Molokai
					hasChildPilot = false;									
					
					if (readyToTerminate) {						
						gameEnd.acquire();
						end.wake();
						gameEnd.release();
						childReadyAtMolokai.sleep();
					}
					else {
						System.out.println("--- child ready on Molokai");
						childReadyAtMolokai.wake();
					}
				}
			}			
			else {
				// Else, if we are currently on Molokai...
				while(boatLocation != 1 || hasChildPilot) {
					System.out.println("--- child currently on Molokai, but either the boat is not on Molokai or there's already a child pilot");
					// ...but the boat is not at Molokai, then sleep.
					childReadyAtMolokai.sleep();
				}
				
				// ...then send a child back to Oahu.	
				hasChildPilot = true;
				System.out.println("--- child currently on Molokai, send one back to Oahu");					
				numChildrenOnMolokai--; // child is going from Molokai --> Oahu, so decrement
				bg.ChildRowToOahu(); 					
				numChildrenOnOahu++; // child arrived at Oahu, so incrememt
				printState();
				boatLocation = 0; // boat is now at Oahu
				currentLocation = 0; // child is now at Oahu
				hasChildPilot = false;
				
				if(numChildrenOnOahu >= 2) {
					System.out.println("--- we now have 2 children on Oahu");
					childReadyAtOahu.wake(); // let Oahu now we're ready to go					
				}
				else {					
					System.out.println("--- we don't enough children on Oahu so send an adult");
					adultReadyAtOahu.wake();	
					childReadyAtOahu.sleep();
				}				
			}
		}		
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}
	
	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n --- Testing boat problem with 2 children, 0 adults.");
		begin(0, 2, b);

		System.out.println("\n --- Testing boat problem with 2 children, 1 adult.");
		begin(1, 2, b);

		System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		begin(3, 3, b);
	}

}
