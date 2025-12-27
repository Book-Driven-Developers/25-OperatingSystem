public final class CountingSemaphore {
    private int permits;

    public CountingSemaphore(int initialPermits) {
        if (initialPermits < 0) throw new IllegalArgumentException("permits < 0");
        this.permits = initialPermits;
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    public synchronized void release() {
        permits++;
        notifyAll();
    }

    public synchronized int availablePermits() {
        return permits;
    }
}
