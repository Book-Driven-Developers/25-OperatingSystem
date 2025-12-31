package os.deadlock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 데드락을 회피하는 방식(자원의 부족이 일어나지 않는 상황을 만듬)
/*
* Reentrant 는 실패 감지가 가능하고 점유 + 대기 차단이 가능하다.
* ReentrantLock은
락을 실제로 “기다리며 들어가지 않고”,
들어갈 수 있는지 먼저 확인해서
위험하면 아예 진입하지 않음으로써
데드락을 회피한다.
* 내부적으로 state값이 있는데 이 값이 현재 락을 잡고 있는 스레드의 갯수. 0이 아니면 점유중
*
* */
public class DeadlockAvoidance {

    static final Lock R1 = new ReentrantLock();
    static final Lock R2 = new ReentrantLock();

    public static void main(String[] args) {

        Thread t1 = new Thread(new Worker("T1", R1, R2));
        Thread t2 = new Thread(new Worker("T2", R2, R1));

        t1.start();
        t2.start();
    }
}

class Worker implements Runnable {

    private final String name;
    private final Lock first;
    private final Lock second;

    public Worker(String name, Lock first, Lock second) {
        this.name = name;
        this.first = first;
        this.second = second;
    }

    @Override
    public void run() {
        while (true) {
            if (first.tryLock()) {
                try {
                    System.out.println(name + ": 첫 번째 자원 획득");
                    sleep();

                    if (second.tryLock()) {
                        try {
                            System.out.println(name + ": 두 번째 자원 획득");
                            System.out.println(name + ": 작업 완료");
                            break;
                        } finally {
                            second.unlock();
                        }
                    }
                } finally {
                    first.unlock();
                }
            }

            System.out.println(name + ": 자원 확보 실패 -> 다시 시도");
            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }
}
