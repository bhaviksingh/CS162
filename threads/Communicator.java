package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	
	private Lock lock;	
    private Condition2 speakCondition;
    private Condition2 listenCondition;      
    private KThread currentSpeaker;
    private KThread currentListener;
    private int msg;
    
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	lock = new Lock();    	
    	speakCondition = new Condition2(lock);
    	listenCondition = new Condition2(lock);    	   
    	currentSpeaker = null;
    	currentListener = null;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {    
    	//System.out.println("--- called speak() with the msg "+word);
    	lock.acquire();
    	
    	while(currentSpeaker != null) {
    		speakCondition.sleep();
    	}    	    
    	
    	//System.out.println("--- reached checkpoint 1 in speak()");
    	
    	msg = word;
    	currentSpeaker = KThread.currentThread();
    	
    	while(currentListener == null) {    		
    		listenCondition.wake();
    		speakCondition.sleep();
    	}    	    
    	
    	speakCondition.wake();
    	listenCondition.wake();   
    	
    	//System.out.println("--- reached checkpoint 2 in speak()");
    	currentListener = null;
    	
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {    	
    	//System.out.println("--- called listen()");
    	lock.acquire();
    	
    	while(currentListener != null) {
    		listenCondition.sleep();
    	}
    	
    	//System.out.println("--- reached checkpoint 1 in listen()");
    	
    	currentListener = KThread.currentThread();
    	
    	while(currentSpeaker == null) {    		
    		speakCondition.wake();
    		listenCondition.sleep();
    	}
    	      	    	
    	speakCondition.wake();
    	listenCondition.wake();       	
    	
    	lock.release();  
    	
    	currentSpeaker = null;
    	//System.out.println("--- returning from listen() with the msg "+msg);
    	
    	return msg;
    }       
    
    public static void selfTest() {
    	final Communicator com = new Communicator();
    	
    	KThread thread1 = new KThread(new Runnable() {
		    public void run() {		    	
		    	System.out.println("--- Thread 1 has begun speaking (msg = 1).");
		    	com.speak(1);		    	
		        System.out.println("--- Thread 1 finished speaking.");
		    }
		});

		KThread thread2 = new KThread(new Runnable() {
		    public void run() {
		         System.out.println("--- Thread 2 has begun listening.");
		         com.listen();
		         System.out.println("--- Thread 2 finished listening.");		         
		    }
		});
		
		KThread thread3 = new KThread(new Runnable() {
			public void run() {
				System.out.println("--- Thread 3 has begun speaking (msg = 3).");
				com.speak(3);
				System.out.println("--- Thread 3 finished speaking.");
			}
		});
		
		KThread thread4 = new KThread(new Runnable() {
			public void run() {
				System.out.println("--- Thread 4 has begun listening.");
				com.listen();
				System.out.println("--- Thread 4 finished listening.");
			}
		});

		thread1.fork();
		thread2.fork();
		thread3.fork();
		thread4.fork();
		thread1.join();
		thread2.join();   
		thread3.join();
		thread4.join();
    }
}
