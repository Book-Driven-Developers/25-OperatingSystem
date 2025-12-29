package ch12.sync;

import java.util.concurrent.Semaphore;

public class SemaphoreStrategy implements SyncStrategy {
    private final Semaphore semaphore;

    public SemaphoreStrategy(int permits, boolean fair) {
        this.semaphore = new Semaphore(permits, fair);
    }

    @Override
    public void withLock(Runnable task) {
        try {
            semaphore.acquire();
            try {
                task.run();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

