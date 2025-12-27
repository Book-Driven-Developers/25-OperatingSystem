package ch12;

import ch12.sync.SyncStrategy;

public class Counter {
    private final SyncStrategy sync;

    private int value = 0;

    public Counter(SyncStrategy sync) {
        this.sync = sync;
    }

    public int get() {
        return value;
    }

    public void increment() {
        sync.withLock(() -> {
            value = value + 1;
        });
    }
}
