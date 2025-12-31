package os.deadlock;

// 데드락의 4가지 조건을 구현해본다.
/*
 * 상호 배제 - synchronized
 * 점유 후 대기 - R1 쥔 채 R2 대기
 * 비선점 - 락 강제 회수 기능 X
 * 순환 대기 - T1 -> R2 -> T2 -> R1
 * */
// 두 개의 자원을 두고 프로세스 두 개가 한개씩 자원을 점유하고 서로 무한정 대기하는 프로그램
public class DeadlockStatus {

    static final Object R1 = new Object();
    static final Object R2 = new Object();

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            synchronized (R1) { // 자바의 객체는 모니터락을 하나씩 가지고 있는데 synchronized는 그 객체의 락을 사용함.
                System.out.println("T1: R1 획득");
                getSleep();

                synchronized (R2) {
                    System.out.println("T1: R2 획득");
                    getSleep();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (R2) {
                System.out.println("T2: R2 획득");
                getSleep();

                synchronized (R1) {
                    System.out.println("T2: R1 획득");
                    getSleep();
                }
            }
        });

        t1.start();
        t2.start();
    }

    private static void getSleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
    }


}
