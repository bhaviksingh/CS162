package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Comparator;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    	initDefaults(1,0,7);
    }
    
    protected static void initDefaults(int a, int b, int c){
    	priorityDefault = a;
    	priorityMinimum = b;
    	priorityMaximum = c;
    }
    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static int priorityDefault ;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static int priorityMinimum ;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static int priorityMaximum ;   
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	
	getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMaximum)
	    return false;

	setPriority(thread, priority+1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    public boolean decreasePriority() {
	boolean intStatus = Machine.interrupt().disable();
		       
	KThread thread = KThread.currentThread();

	int priority = getPriority(thread);
	if (priority == priorityMinimum)
	    return false;

	setPriority(thread, priority-1);

	Machine.interrupt().restore(intStatus);
	return true;
    }

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
	
		PriorityQueue(boolean transferPriority) {
		    this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).waitForAccess(this); //threadState.waitForAcess(me who is queue)
		}

		public void acquire(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    if(!orderedThreads.isEmpty()){
		    	//System.out.println("Asking for nexthread, ordered threads is " + orderedThreads);
		    	//System.out.println("Top is " + orderedThreads.peek());
			    KThread nextThread = orderedThreads.poll().thread;
			    acquire(nextThread);
			    return this.lockHolder.thread; //this assumes that its worked
		    } 
		    return null;
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
			return orderedThreads.peek();
		}
		
		public void print() {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    // implement me (if you want)
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		private java.util.PriorityQueue<ThreadState> orderedThreads = new java.util.PriorityQueue<ThreadState>(10, new ThreadComparator());
		protected ThreadState lockHolder = null;
		
		protected class ThreadComparator implements Comparator<ThreadState> {
			@Override
			public int compare(ThreadState t1, ThreadState t2) {
				if (t1.getEffectivePriority() > t2.getEffectivePriority()) {
					return -1;
				} else if (t1.getEffectivePriority() < t2.getEffectivePriority()) {
					return 1;
				} else {
					if (t1.getWaitingTime() < t2.getWaitingTime()) {
						return -1;
					} else if(t1.getWaitingTime() > t2.getWaitingTime()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		}

		public void addState(ThreadState thread) {
			this.orderedThreads.add(thread);
		}
		
		public void removeState(ThreadState thread) {
			this.orderedThreads.remove(thread);
		}
	}

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
		    this.thread = thread;
		    setPriority(priorityDefault);
		}

		//getter methods for the comparator
		public long getWaitingTime() {
			return this.waitingTime;
		}

		public Object getWaitingQueue() {
			return this.waitingQueue;
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
		    return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {

		    return effectivePriority;
		}
		
		protected void updateEffectivePriority() {
			
			//System.out.println("Updating priority for "+ this);
			int tempPriority =  0; //start at an incredibly low number
			
			for (PriorityQueue resource: acquired){
				if (resource.transferPriority && resource.orderedThreads.peek() != null){
					//System.out.println("orderedthreads is " + resource.orderedThreads);
					int resourceMax = resource.orderedThreads.peek().getEffectivePriority();
					if (tempPriority < resourceMax) {
						tempPriority = resourceMax;
					}
				}
			}
			
			if (this.effectivePriority != tempPriority){ 
				
				if (this.waitingQueue!= null){
 					waitingQueue.removeState(this);
				}
				
				//if the priority is changed, it could be lowered, but never lower than the priority itself
				if (tempPriority < this.priority) {
					this.effectivePriority = this.priority; //so if tempPriority is 0, this is the case that gets called
				} else {
					this.effectivePriority = tempPriority; //else its gone up!
				}
				
 				if(this.waitingQueue != null) {
 					waitingQueue.addState(this);
 				}

				//if the queue im waiting on exists, propogate iff donation is on!
				if(waitingQueue != null && waitingQueue.lockHolder != null && waitingQueue.lockHolder != this)
				{
					this.waitingQueue.lockHolder.updateEffectivePriority();
				}
			}  
		}
		

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
		    if (this.priority == priority)
			return;

		    this.priority = priority;

		    if (this.waitingQueue != null){
				waitingQueue.removeState(this);
		    }
			
		    if (this.priority > this.effectivePriority) {
		    	this.effectivePriority = this.priority;
		    } else {
		    	this.updateEffectivePriority(); //our priority may be the only thing holding effective up
		    }
		    
			if(this.waitingQueue != null) {
				waitingQueue.addState(this);
			}
		    
		    if(this.waitingQueue != null && this.waitingQueue.lockHolder != null) {
		    	this.waitingQueue.lockHolder.updateEffectivePriority();
		    }
		}

		
		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
		    this.waitingQueue = waitQueue;
		    this.waitingTime = System.currentTimeMillis();
		  
		    waitQueue.addState(this);
		    
		    if (this.waitingQueue != null && this.waitingQueue.lockHolder != null){
		    	this.waitingQueue.lockHolder.updateEffectivePriority();
		    }
		}
		
		
		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		
		public void acquire(PriorityQueue waitQueue) {
		    if(waitQueue.lockHolder != null) {
		    	waitQueue.lockHolder.relinquish(waitQueue);
		    }
		    if (this.waitingQueue == waitQueue){
		    	this.waitingQueue = null;
		    }
		    waitQueue.removeState(this); //if i was on the wait queue, remove me
		    waitQueue.lockHolder = this; //and set me to lockholder
		    this.acquired.add(waitQueue);
		    this.updateEffectivePriority();
		}
		/**
		 * Helper function for acquired.
		 * @param waitQueue will leave the function with no lockholder
		 */
		protected void relinquish(PriorityQueue releasing){
			this.effectivePriority = this.priority; //not sure about this tbh
			this.acquired.remove(releasing);
			releasing.lockHolder = null; //maybe this doesnt need to be there
			if (this.waitingQueue!= null && this.waitingQueue.lockHolder != null){
				this.waitingQueue.lockHolder.updateEffectivePriority();
			}
		}

		public String toString(){
			String s = "{" + this.thread + " e:" + this.effectivePriority + " p:" + this.priority + " w:" + this.waitingTime + "}";
			return s;
		}
		
		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		
		protected int effectivePriority;
		protected PriorityQueue waitingQueue = null;
		protected HashSet<PriorityQueue> acquired = new HashSet<PriorityQueue>();
		
		private long waitingTime = 0;
		
    }
    

}
