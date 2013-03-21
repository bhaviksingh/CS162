package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    	initDefaults(1,1, Integer.MAX_VALUE);
    }
    
    @Override
    protected ThreadState getThreadState(KThread thread) {
    	if (thread.schedulingState == null)
    		thread.schedulingState = new LotteryState(thread);

    	return (ThreadState) thread.schedulingState;
    }


    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	// implement me
	return new LotteryQueue(transferPriority);
    }
    
    protected class LotteryQueue extends PriorityScheduler.PriorityQueue {
    	
    	private LinkedList<ThreadState> lottery;

		public LotteryQueue(boolean transferPriority) {
			super(transferPriority);
			this.lottery = new LinkedList<ThreadState>();
			this.lockHolder = null;
		}

		@Override
		public void addState(ThreadState thread){
			lottery.push(thread);
		}
		
		@Override
		public void removeState(ThreadState thread){
			lottery.remove(thread);
		}
		
		@Override
		public KThread nextThread(){
			if (! lottery.isEmpty()){
				ThreadState t = pickNextThread();
				this.acquire(t.thread);
				return t.thread;
			}
			return null;
		}
		
		@Override 
		public ThreadState pickNextThread(){
			int numTickets = getTotalTickets();
			int rand = (int) (1 + Math.random()*numTickets);
			for (ThreadState t: lottery){
				rand = rand - t.effectivePriority;
				if (rand <= 0){
					return t;
				}
			}
			return null;
		}
		
		private int getTotalTickets(){
			int sum = 0;
			for(ThreadState t: lottery){
				sum += t.effectivePriority;
			}
			return sum;
		}
		
		@Override
		public String toString(){
			return "lottery is " + lottery + " acquired by " + lockHolder;
		}
    	
    }
    
    protected class LotteryState extends PriorityScheduler.ThreadState {
    	
    	
		public LotteryState(KThread thread) {
			super(thread);
		}
		
		@Override
		public String toString(){
			return "p: " + priority + " e: " + effectivePriority + " t: " + thread;
		}
		@Override
		public long getWaitingTime(){
			return 0;
		}
		
		@Override
		protected void updateEffectivePriority(){
			//Do nothing right now;
			int sum = this.priority;
			for (PriorityQueue pq: acquired){
				if(pq.transferPriority){
					LotteryQueue l;
					try {
						l = (LotteryQueue) pq;
						for (ThreadState thread: l.lottery){
							sum += thread.effectivePriority;
						}
					} catch (Exception e) {
						System.out.println("Error in casting pq to lotteryQueue " + e.getMessage());
					}
					
				}
			}
			
			if (sum != this.priority){
				this.effectivePriority = sum;
				if (this.waitingQueue != null && this.waitingQueue.lockHolder != null){
					this.waitingQueue.lockHolder.updateEffectivePriority();
				}
			}
			
		}
    	
    }
    
    public static void selfTest(){
    	System.out.println("Testing lottery");
    	boolean restore = Machine.interrupt().disable();
    	
    	ThreadQueue l1 = ThreadedKernel.scheduler.newThreadQueue(true);
    	KThread k1 = new KThread(), k2 = new KThread(), k3 = new KThread(), k4 = new KThread();
    	k1.setName("k1"); k2.setName("k2"); k3.setName("k3"); k4.setName("k4");
    	l1.waitForAccess(k1);
    	l1.waitForAccess(k2);
    	l1.waitForAccess(k3);
    	
    	KThread laterIncrease;
    	
    	//Basic test
    	System.out.println("\n Basic test");
    	KThread chosen = l1.nextThread();
    	
    	if (chosen != k1){
    		laterIncrease = k1;
    	} else {
    		laterIncrease = k2;
    	}
    	
    	System.out.println("After initial 3 add, chose: " + chosen);
    	System.out.println("q is " + l1);
    	
    	//high priority test
    	System.out.println("\n High priority test");
    	ThreadedKernel.scheduler.setPriority(k4, 100);
    	l1.waitForAccess(k4);
    	System.out.println("K4 added now we have" + l1);
    	System.out.println("Chose next thread (k4?) " + l1.nextThread());
    	System.out.println("After choose its " + l1);
    	
    	//propogation test
    	System.out.println("\n Propogation test");
    	ThreadQueue l2 = ThreadedKernel.scheduler.newThreadQueue(true);
    	KThread a1 = new KThread();
    	a1.setName("a");
    	l2.acquire(a1);
    	System.out.println("After a acquires l2 "+ l2);
    	l2.waitForAccess(k4);
    	System.out.println("After k4 waits on l2 " + l2);
    	
    	ThreadedKernel.scheduler.setPriority(laterIncrease, 80);
    	System.out.println("set " + laterIncrease + " priority to 80, now we have \nl1: " + l1 + "\nl2: " + l2 );
    	
    	
    }
}
