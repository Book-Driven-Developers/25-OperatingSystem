package ch12.sync;

public class NoStrategy implements SyncStrategy {
    @Override
    public void withLock(Runnable task) {
        task.run();
    }
}

