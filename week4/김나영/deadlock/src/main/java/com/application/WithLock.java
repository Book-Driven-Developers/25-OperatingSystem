package com.application;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WithLock {
    public static final Lock lock1 = new ReentrantLock();
    public static final Lock lock2 = new ReentrantLock();

    public static void main(String[] args) {
        FirstThread firstThread = new FirstThread();
        SecondThread secondThread = new SecondThread();

        firstThread.start();
        secondThread.start();
    }

    public static class FirstThread extends Thread {
        @Override
        public void run() {
            while (true) {
                boolean gotLock1 = false;
                boolean gotLock2 = false;

                try {
                    // lock1 획득 시도 (100ms 타임아웃)
                    gotLock1 = lock1.tryLock(100, TimeUnit.MILLISECONDS);

                    if (!gotLock1) {
                        System.out.println("First Thread - lock1 획득 실패, 재시도...");
                        continue;  // 다시 시도
                    }

                    System.out.println("First Thread - lock1 획득 성공");

                    // 잠시 대기 (교착 상태 시뮬레이션)
                    Thread.sleep(10);

                    System.out.println("First Thread - lock2 획득 시도...");

                    // lock2 획득 시도 (100ms 타임아웃)
                    gotLock2 = lock2.tryLock(100, TimeUnit.MILLISECONDS);

                    if (!gotLock2) {
                        System.out.println("First Thread - lock2 획득 실패, lock1 해제 후 재시도...");
                        continue;  // lock1도 자동으로 해제됨 (finally 블록에서)
                    }

                    System.out.println("First Thread - lock2 획득 성공");

                    // 실제 작업 수행
                    System.out.println("First Thread - 작업 완료!");
                    break;  // 성공, 루프 탈출

                } catch (InterruptedException e) {
                    System.out.println("First Thread - 인터럽트 발생");
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    // 획득한 락들을 역순으로 해제
                    if (gotLock2) {
                        lock2.unlock();
                        System.out.println("First Thread - lock2 해제");
                    }
                    if (gotLock1) {
                        lock1.unlock();
                        System.out.println("First Thread - lock1 해제");
                    }
                }
            }
        }
    }

    public static class SecondThread extends Thread {
        @Override
        public void run() {
            while (true) {
                boolean gotLock2 = false;
                boolean gotLock1 = false;

                try {
                    // lock2 획득 시도
                    gotLock2 = lock2.tryLock(100, TimeUnit.MILLISECONDS);

                    if (!gotLock2) {
                        System.out.println("Second Thread - lock2 획득 실패, 재시도...");
                        continue;
                    }

                    System.out.println("Second Thread - lock2 획득 성공");

                    Thread.sleep(10);

                    System.out.println("Second Thread - lock1 획득 시도...");

                    // lock1 획득 시도
                    gotLock1 = lock1.tryLock(100, TimeUnit.MILLISECONDS);

                    if (!gotLock1) {
                        System.out.println("Second Thread - lock1 획득 실패, lock2 해제 후 재시도...");
                        continue;
                    }

                    System.out.println("Second Thread - lock1 획득 성공");

                    // 실제 작업 수행
                    System.out.println("Second Thread - 작업 완료!");
                    break;

                } catch (InterruptedException e) {
                    System.out.println("Second Thread - 인터럽트 발생");
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    // 획득한 락들을 역순으로 해제
                    if (gotLock1) {
                        lock1.unlock();
                        System.out.println("Second Thread - lock1 해제");
                    }
                    if (gotLock2) {
                        lock2.unlock();
                        System.out.println("Second Thread - lock2 해제");
                    }
                }
            }
        }
    }
}
