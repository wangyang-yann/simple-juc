package test;

import locks.ReentrantLock;
//import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

    public ReentrantLock lock;

    private int num = 0;

    public int getNum(){
        return num;
    }

    public void setNum(int num){
        this.num = num;
    }

    public ReentrantLockTest(){
        this.lock = new ReentrantLock();
    }

    public ReentrantLockTest(boolean fair){
        this.lock = new ReentrantLock(fair);
    }
}
