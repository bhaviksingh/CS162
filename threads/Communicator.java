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
    private boolean receivedMsg;
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
    	receivedMsg = false;
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
    	lock.acquire();
    	
    	while(currentSpeaker != null) {
    		speakCondition.sleep();
    	}    	    
    	
    	msg = word;
    	currentSpeaker = KThread.currentThread();
    	
    	while(currentListener == null || !receivedMsg) {    		
    		listenCondition.wake();
    		speakCondition.sleep();
    	}    	    
    	
    	receivedMsg = false;
    	speakCondition.wake();
    	listenCondition.wake();    	
    	
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	lock.acquire();
    	
    	while(currentListener != null) {
    		listenCondition.sleep();
    	}
    	
    	currentListener = KThread.currentThread();
    	
    	while(currentSpeaker == null) {    		
    		speakCondition.wake();
    		listenCondition.sleep();
    	}
    	      	
    	receivedMsg = true; 
    	speakCondition.wake();
    	listenCondition.wake();    	       	
    	
    	lock.release();    	    
    	
    	return msg;
    }       
    
    public static void selfTest() {
    	final Communicator commu = new Communicator();

		KThread thread2 = new KThread(new Runnable() {
		    public void run() {
		         System.out.println("--- Thread 2 has begun listening.");
		         commu.listen();
		         System.out.println("--- Thread 2 finished listening.");		         
		    }
		});

		KThread thread1 = new KThread(new Runnable() {
		    public void run() {
		    	int toSend = 8;
		    	System.out.println("--- Thread 1 has begun speaking.");
		    	System.out.println("--- Attempting to send the message "+toSend+".");
		    	commu.speak(toSend);
		        System.out.println("--- Thread 1 finished speaking.");
		    }
		});

		thread1.fork();
		thread2.fork();
		thread1.join();
		thread2.join();    
    }
}
