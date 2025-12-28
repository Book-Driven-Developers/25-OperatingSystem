package os.monitor;

import java.util.Arrays;

/*
Java의 `synchronized` + `wait/notify` 자체가 모니터 매커니즘입니다.
가장 전형적인 예로 생산자-소비자를 모니터로 구현합니다.

- 이 클래스는 lock(상호 배제): synchronized가 보장
- condition(조건 대기/신호): wait/notifyAll()이 보장
 */
public final class BoundedBuffer<T> {
    private final Object[] items;
    private int putIndex = 0;
    private int takeIndex = 0;
    private int count = 0;

    public BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        this.items = new Object[capacity];
    }

    public synchronized void put(T x) throws InterruptedException {
        while (count == items.length) {
            wait(); // 버퍼가 꽉 차면 생산자는 대기
        }
        items[putIndex] = x;
        putIndex = (putIndex + 1) % items.length;
        count++;
        notifyAll(); // 소비자 깨우기
    }

    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (count == 0) {
            wait(); // 버퍼가 비면 소비자는 대기
        }
        Object x = items[takeIndex];
        items[takeIndex] = null;
        takeIndex = (takeIndex + 1) % items.length;
        count--;
        notifyAll(); // 생산자 깨우기
        return (T) x;
    }

    @Override
    public String toString() {
        return "BoundedBuffer{items=" + Arrays.toString(items) +
                ", count=" + count +
                '}';
    }
}
