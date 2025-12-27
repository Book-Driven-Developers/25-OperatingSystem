package ch12.sync;

public interface SyncStrategy {
    void withLock(Runnable task);
}

