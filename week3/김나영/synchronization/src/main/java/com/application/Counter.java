package com.application;

public class Counter {
    private int count = 0;
    private final Mutex mutex = new Mutex();

    public void increment() {
        try {
            mutex.acquire();
            // 임계 영역
            int temp = count;
            Thread.sleep(1); // 일부러 지연
            count = temp + 1;
            mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return count;
    }
}
