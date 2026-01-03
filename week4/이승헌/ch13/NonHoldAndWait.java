package ch13;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NonHoldAndWait {
    private final Lock lock1 = new ReentrantLock();
    private final Lock lock2 = new ReentrantLock();

    public void run() throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            try {
                if (lock1.tryLock()) {
                    System.out.println("스레드 1: 락 1을 잡고 있습니다...");
                    Thread.sleep(100);
                    if (lock2.tryLock()) {
                        try {
                            System.out.println("스레드 1: 락 2를 획득했습니다!");
                        } finally {
                            lock2.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock1.unlock();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                if (lock2.tryLock()) {
                    System.out.println("스레드 2: 락 2를 잡고 있습니다...");
                    Thread.sleep(100);
                    if (lock1.tryLock()) {
                        try {
                            System.out.println("스레드 2: 락 1을 획득했습니다!");
                        } finally {
                            lock1.unlock();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock2.unlock();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("교착 상태 없음: 스레드가 성공적으로 완료되었습니다.");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[ NonHoldAndWait ]");
        new NonHoldAndWait().run();
    }
}
