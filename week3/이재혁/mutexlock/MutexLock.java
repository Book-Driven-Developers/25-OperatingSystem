package os.mutexlock;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/*
- 한 번에 오직 1개 스레드만 임계 구역(critical section)에 들어가게 하는 락
- 아래 구현은 AtomicBoolean(해당 변수에 동기작업만 허용) + LockSupport.park/unpark + 대기 큐를 이용한 간단한 뮤텍스 락
- 스프링 프로젝트는 스프링 서버가 하나의 프로세스가 되고 자식 프로세스를 만드는 구조가 아닌 멀티 스레드를 활용하는 방식
 */
public final class MutexLock {

    private final AtomicBoolean locked = new AtomicBoolean(false); // 뮤텍스 락 용 글로벌 락
    private final ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>(); // 대기중인 스레드를 처리하기 위한 큐, 여러 스레드가 공유하는 코드
    private volatile Thread lock_owner = null;

    public void lock() {
        final Thread current = Thread.currentThread(); // 현재 이 코드를 실행하고 있는 스레드의 정보를 가져와 저장
        waiters.add(current);

        // 내 차례(큐 맨 앞) 이고, 락을 획득할 수 있을 때까지 대기
        while (waiters.peek() != current || !locked.compareAndSet(false, true)) {
            LockSupport.park(this); // 현재 실행 중인 스레드를 대기(wait) 상태로 전환
        }

        // 락 획득 성공 -> 큐에서 제거
        waiters.remove();
        lock_owner = current;
    }

    public void unlock() {
        if (Thread.currentThread() != lock_owner) {
            throw new IllegalMonitorStateException("락을 소유하고 있지 않습니다.");
        }

        // 락 해제
        lock_owner = null;
        locked.set(false);

        // 다음 대기자 깨우기
        Thread next = waiters.peek();
        if (next != null) {
            LockSupport.unpark(next);
        }
    }
}
