package os.semaphore;

/*
- 허가된 갯수 S 만큼 동시에 S개가 들어오도록 제한
- 대표적으로 동시 호출 제한 / 동시 처리량 제한에 씁니다.
 */
public final class CountingSemaphore {

    private int s; // 세마포어 상태 변수 S

    public CountingSemaphore(int s) {
        if (s < 0) throw new IllegalArgumentException("s < 0");
        this.s = s;
    }

    public synchronized void acquire() throws InterruptedException {
        while (s == 0) {
            wait();
        }
        s--;
    }

    public synchronized void release() {
        s++;
        notifyAll();
    }

    public synchronized int availablePermits() {
        return s;
    }
}
