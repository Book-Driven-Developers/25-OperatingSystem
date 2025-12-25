package com.application;

import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
    private int S;
    private Queue<Thread> queue;

    public Semaphore(int S) {
        this.S = S;
        this.queue = new LinkedList<>();
    }

    public synchronized void wait_sempahore() throws InterruptedException {
        S--;

        if (S < 0) {
            queue.add(Thread.currentThread());
            wait();
        }
    }

    public synchronized void signal() {
        S++;
        if (S <= 0) {
            Thread t = queue.poll();
            if (t != null) {
                notify();
            }
        }
    }
}
