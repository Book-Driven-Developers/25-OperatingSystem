package ch12.sync;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexStrategy implements SyncStrategy {
    private final Lock lock;

    public MutexStrategy() {
        this(true);
    }

    public MutexStrategy(boolean fair) {
        this.lock = new ReentrantLock(fair);
    }

    @Override
    public void withLock(Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }
}

