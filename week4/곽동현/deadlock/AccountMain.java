package operation_system.cpu_scheduling.thread.deadlock;

public class AccountMain {
    public static void main(String[] args) throws InterruptedException {
        AccountService accountService = new AccountService();
        Account accountA = new Account("A", 1000);
        Account accountB = new Account("B", 1000);

        // 1. 데드락 발생
//        // 스레드 1: A → B 이체
//        new Thread(() -> accountService.transfer(accountA, accountB, 100)).start();
//
//        // 스레드 2: B → A 이체 (락 순서 반대)
//        new Thread(() -> accountService.transfer(accountB, accountA, 200)).start();

        // 2. 데드락 회피
        new Thread(() -> accountService.avoidTransferDeadLock(accountA,accountB,100)).start();
        new Thread(() -> accountService.avoidTransferDeadLock(accountB,accountA,200)).start();
    }
}
