package ch13;

public class NonCircularWait {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void run() {
        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("스레드 1: 락 1을 잡고 있습니다...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2) {
                    System.out.println("스레드 1: 락 2를 획득했습니다!");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("스레드 2: 락 1을 잡고 있습니다...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2) {
                    System.out.println("스레드 2: 락 2를 획득했습니다!");
                }
            }
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("교착 상태 없음: 스레드가 성공적으로 완료되었습니다.");
    }

    public static void main(String[] args) {
        System.out.println("[ NonCircularWait ]");
        new NonCircularWait().run();
    }
}
