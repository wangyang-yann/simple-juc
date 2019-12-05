package test;

public class Main {

    public static void main(String[] args){
        ReentrantLockTest reentrantLockTest = new ReentrantLockTest(true);
        Thread t1 = new Thread(getTask(reentrantLockTest));
        Thread t2 = new Thread(getTask(reentrantLockTest));
        Thread t3 = new Thread(getTask(reentrantLockTest));
        t1.start();
        t2.start();
        t3.start();
    }

    private static Runnable getTask(ReentrantLockTest reentrantLockTest){
        return () -> {
            while(reentrantLockTest.getNum()<1000){
//                System.out.println(Thread.currentThread().getName()+" running");
                reentrantLockTest.lock.lock();
                try{
                    System.out.println(Thread.currentThread().getName()+"num:"+reentrantLockTest.getNum());
                    reentrantLockTest.setNum(reentrantLockTest.getNum()+1);
//                    if (reentrantLockTest.getNum()%10==0){
//                        reentrantLockTest.lock.tryLock();
//                        System.out.println(Thread.currentThread().getName()+" reentry!");
//                        reentrantLockTest.lock.unlock();
//                    }
                } finally {
                    reentrantLockTest.lock.unlock();
                }
            }
        };
    }
}
