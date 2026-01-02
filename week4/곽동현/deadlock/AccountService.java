package operation_system.cpu_scheduling.thread.deadlock;

public class AccountService {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    // 동시수행
    // 스레드 A: 계좌 A -> B 이체
    // 스레드 B: 계좌 B -> A 이체
    public void transfer(Account from, Account to, int amount) {
        synchronized (from) {  // from 계좌 락 획득
            System.out.println("acquired first Lock " + from.getId());

            // 락 점유 시간 증가
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            synchronized (to) {  // to 계좌 락 시도
                System.out.println("acquired second Lock" + to.getId());
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    // 데드락 회피
    public void avoidTransferDeadLock(Account from, Account to, int amount) {
        // 항상 ID가 작은 계좌부터 락 획득 (순서 통일)
        Account first = from.getId().compareTo(to.getId()) < 0 ? from : to;
        Account second = from.getId().compareTo(to.getId()) < 0 ? to : from;

        synchronized (first) {
            System.out.println("acquired first Lock " + from.getId());
            synchronized (second) {
                System.out.println("acquired second Lock " + to.getId());
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }
}
