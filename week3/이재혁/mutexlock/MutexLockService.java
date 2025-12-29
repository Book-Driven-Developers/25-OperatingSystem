package os.mutexlock;

public class MutexLockService {

    private final MutexLock lock = new MutexLock();
    private int count = 0; // 공유 자원(shared resource)

    public int incrementAndGet() {
        lock.lock();
        try {
            return ++count;
        } finally {
            lock.unlock();
        }
    }
}
