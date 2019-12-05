package locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ReentrantLock implements Lock, java.io.Serializable {

    private boolean fair = false;

    private FairSync fairSync = new FairSync();

    private Sync unFairSync = new UnfairSync();

    public ReentrantLock(){}

    public ReentrantLock(boolean fair){
        this.fair = fair;
    }

    private abstract static class Sync extends AbstractQueuedSynchronizer {

        void acquire(){
            acquire(1);
        }

        boolean tryAcquire(){
            return tryAcquire(1);
        }

        void release(){
            release(1);
        }

        @Override
        protected boolean tryRelease(int releaseTimes){
            int c = getState() - releaseTimes;
            if (Thread.currentThread()!=getExclusiveOwnerThread()){
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
    }

    private static class FairSync extends Sync {

        @Override
        protected boolean tryAcquire(int acquireTimes){
            int state = getState();
            if (state==0){
                if (!hasQueuedPredecessors() && compareAndSetState(0,acquireTimes)){
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            } else if (Thread.currentThread()==getExclusiveOwnerThread()){
                if (state+acquireTimes<0){
                    throw new Error("Maximum lock count exceed!");
                }
                setState(state+acquireTimes);
                return true;
            }
            return false;
        }
    }

    private static class UnfairSync extends Sync {

        @Override
        protected boolean tryAcquire(int acquireTimes){
            int state = getState();
            if (state==0){
                if (compareAndSetState(0,acquireTimes)){
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            } else if (Thread.currentThread() == getExclusiveOwnerThread()){
                if (state+acquireTimes<0){
                    throw new Error("Maximum lock count exceed!");
                }
                setState(state+acquireTimes);
                return true;
            }
            return false;
        }
    }


    @Override
    public void lock() {
        if (fair){
            fairSync.acquire();
        } else {
            unFairSync.acquire();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return fair?fairSync.tryAcquire():unFairSync.tryAcquire();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        if (fair){
            fairSync.release();
        } else {
            unFairSync.release();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
