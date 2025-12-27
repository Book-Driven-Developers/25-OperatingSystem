package ch12.sync;

public class MonitorStrategy implements SyncStrategy {
    private final Object lock = new Object();

    @Override
    public void withLock(Runnable task) {
        synchronized (lock) {
            task.run();
        }
    }
}

