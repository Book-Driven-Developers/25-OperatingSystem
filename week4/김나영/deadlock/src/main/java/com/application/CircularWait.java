package com.application;

public class CircularWait {
    public static Object object1 = new Object();
    public static Object object2 = new Object();

    public static void main(String[] args) {
        Main.FirstThread firstThread = new Main.FirstThread();
        Main.SecondThread secondThread = new Main.SecondThread();

        firstThread.start();
        secondThread.start();

    }

    public static class FirstThread extends Thread {
        @Override
        public void run() {
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

    public static class SecondThread extends Thread {

        // 1. 순환 대기 제거 : 락 획득 순서 통일
        @Override
        public void run() {
            synchronized (object1) {
                System.out.println("Second Thread - object1 locked");

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Second Thread want to have object2's lock");

                synchronized (object2) {
                    System.out.println("Second Thread - object2 locked");
                }
            }
            /* 교착 상태
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
             */
        }
    }
}
