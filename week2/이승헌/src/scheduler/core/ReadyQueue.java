package scheduler.core;

import scheduler.model.PCB;
import scheduler.policy.SchedulingPolicy;

import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ReadyQueue {
    private final PriorityQueue<PCB> pq;
    private final ReentrantLock lock = new ReentrantLock();

    public ReadyQueue(SchedulingPolicy policy) {
        this.pq = new PriorityQueue<>(policy.comparator());
    }

    public void add(PCB pcb) {
        lock.lock();
        try {
            pq.add(pcb);
        } finally {
            lock.unlock();
        }
    }

    public PCB poll() {
        lock.lock();
        try {
            return pq.poll();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return pq.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}

