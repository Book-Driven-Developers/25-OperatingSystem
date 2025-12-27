public final class MutexLock {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();

    public void lock() {
        final Thread current = Thread.currentThread();
        waiters.add(current);

        while (waiters.peek() != current || !locked.compareAndSet(false, true)) {
            LockSupport.park(this);
        }

        waiters.remove();
    }

    public void unlock() {
        locked.set(false);

        Thread next = waiters.peek();
        if (next != null) {
            LockSupport.unpark(next);
        }
    }
}
