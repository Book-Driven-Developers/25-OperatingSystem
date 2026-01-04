package com.application;

public class HoldandWait {

    public static Object object1 = new Object();
    public static Object object2 = new Object();

    public static void main(String[] args) {
        FirstThread firstThread = new FirstThread();
        SecondThread secondThread = new SecondThread();

        firstThread.start();
        secondThread.start();

    }

    public static class FirstThread extends Thread {
        @Override
        public void run() {
            // 두 개의 락을 동시에 획득할 수 있는 상위 락 사용
            synchronized (HoldandWait.class) {
                synchronized (object1) {
                    System.out.println("First Thread - object1 locked");

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("First Thread want to have object2's lock");

                    synchronized (object2) {
                        System.out.println("First Thread - object2 locked");
                    }
                }
            }
        }
    }

    public static class SecondThread extends Thread {
        @Override
        public void run() {
            synchronized (HoldandWait.class) {
                synchronized (object2) {
                    System.out.println("Second Thread - object2 locked");

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Second Thread want to have object1's lock");

                    synchronized (object1) {
                        System.out.println("Second Thread - object1 locked");
                    }
                }
            }

        }
    }
}
