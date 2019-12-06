package locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * state-高16位储存写锁状态，低16位储存读锁状态
 */
public class ReentrantReadWriteLock implements ReadWriteLock {

    private ReadLock readLock;

    private WriteLock writeLock;

    static class Sync extends AbstractQueuedSynchronizer {


        private int getReadState(int state){
            return state >> 16;
        }

        private int getWriteState(int state){
            return state & ((1<<16)-1);
        }

        @Override
        protected boolean tryAcquire(int acquireTimes){
            int state = getState();
            int r = getReadState(state);
            int w = getWriteState(state);
            if (state==0){
                if (compareAndSetState(0,acquireTimes)){
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            } else {
                if (w!=0 && Thread.currentThread()==getExclusiveOwnerThread()){
                    if (state+acquireTimes<0){
                        throw new Error("max write cnt exceed!");
                    }
                    setState(state+acquireTimes);
                }
                return false;
            }
        }

        @Override
        protected int tryAcquireShared(int acquireTimes){
            int state = getState();
            int w = getWriteState(state);
            if (w==0){
                if (state+(acquireTimes<<16)<0){
                    throw new Error("max read cnt exceed!");
                }
                if (compareAndSetState(state,state+(acquireTimes<<16))){
                    return 1;
                }
                return -1;
            } else {
                if (Thread.currentThread()==getExclusiveOwnerThread()){
                    return 0;
                }
                return -1;
            }
        }

    }

    static class ReadLock implements Lock{

        Sync sync;

        ReadLock(Sync sync){
            this.sync = sync;
        }

        @Override
        public void lock() {
            sync.acquireShared(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return sync.tryAcquireShared(1)>=0;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    static class WriteLock implements Lock {

        Sync sync;

        WriteLock(Sync sync){
            this.sync = sync;
        }

        @Override
        public void lock() {
            sync.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    public ReentrantReadWriteLock(){
        Sync sync = new Sync();
        readLock = new ReadLock(sync);
        writeLock = new WriteLock(sync);
    }


    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }
}
