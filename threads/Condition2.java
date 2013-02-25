package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue; // not sure if we use this one or the one we implement in 5?

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
    	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

    	conditionLock.release();
    	
    	boolean intStatus = Machine.interrupt().disable();
    	waitQueue.add(KThread.currentThread());
    	KThread.sleep();
    	Machine.interrupt().restore(intStatus);	

    	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	boolean intStatus = Machine.interrupt().disable();    	
    	if(!waitQueue.isEmpty() && waitQueue.peek() != null) {
    		waitQueue.poll().ready();
    	}  
    	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	boolean intStatus = Machine.interrupt().disable();
    	while(!waitQueue.isEmpty()) {
    		wake();
    	}
    }

    private Lock conditionLock;
    private PriorityQueue<KThread> waitQueue = new PriorityQueue<KThread>();
}
