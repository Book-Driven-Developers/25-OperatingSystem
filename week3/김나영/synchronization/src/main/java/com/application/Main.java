package com.application;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        testMutex();
        System.out.println();
        testSemaphore();
    }

    static void testMutex() throws InterruptedException {
        System.out.println("Mutex");

        Counter counter = new Counter();

        int threadCount = 10;
        int repeat = 100;

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < repeat; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("최종 count 값: " + counter.getCount());
        System.out.println("기대 값: " + (threadCount * repeat));
    }

    static void testSemaphore() {
        System.out.println("Semaphore");
        Semaphore semaphore = new Semaphore(2); // 동시에 2개만 허용

        for (int i = 1; i <= 5; i++) {
            int threadId = i;

            new Thread(() -> {
                try {
                    System.out.println("Thread-" + threadId + " : 대기");
                    semaphore.wait_sempahore();

                    // 임계 영역
                    System.out.println("Thread-" + threadId + " : 실행 중");
                    Thread.sleep(2000);

                    System.out.println("Thread-" + threadId + " : 종료");
                    semaphore.signal();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}