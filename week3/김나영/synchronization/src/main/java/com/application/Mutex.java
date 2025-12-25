package com.application;

import java.util.LinkedList;
import java.util.Queue;

public class Mutex {
    private boolean lock = false;
    private Thread owner = null;
    private Queue<Thread> waitQueue = new LinkedList<>();

    public synchronized void acquire() throws InterruptedException {
        while (lock == true) {
            waitQueue.add(Thread.currentThread());
            wait();  // 잠들기
        }
        lock = true;
        owner = Thread.currentThread();
    }

    public synchronized void release() throws IllegalStateException {
        if (owner != Thread.currentThread()) {
            throw new IllegalStateException("잠금을 획득한 스레드만 해제 가능!");
        }
        lock = false;
        owner = null;
        if (!waitQueue.isEmpty()) {
            waitQueue.poll();
            notify();
        }
    }
}
