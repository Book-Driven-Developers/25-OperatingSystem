package operation_system.cpu_scheduling.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// 해결 방안 1 : 뮤텍스 락
// 특징 : lock 을 잡는 스레드는 반드시 1개

// 구현 방법 : synchronized / ReentrantLock  (사실 살 둘 다 모니터 락에 속함)
// 차이점 : 비공정락 / 공정락(default:비공정)
// 관리 주체: jvm 구현 / 라이브러리
// wait/notify : 자동 / 수동
public class raceConditionMain1 {

    static Map<String, Integer> sharedMap = new HashMap<>();
    static final String KEY = "count";
    static ReentrantLock lock = new ReentrantLock(true);

    public static void main(String[] args) throws InterruptedException {
        sharedMap.put(KEY, 0); // 초기값

        int threadCount = 100;

        Thread[] plusThreads = new Thread[threadCount];
        Thread[] minusThreads = new Thread[threadCount];

        // +1 그룹
        for (int i = 0; i < threadCount; i++) {
            plusThreads[i] = new Thread(() -> raceCondition(+1));
        }

        // -1 그룹
        for (int i = 0; i < threadCount; i++) {
            minusThreads[i] = new Thread(() -> raceCondition(-1));
        }

        long startTime = System.currentTimeMillis();

        // 동시에 시작
        for (int i = 0; i < threadCount; i++) {
            plusThreads[i].start();
            minusThreads[i].start();
        }

        // 종료 대기
        for (int i = 0; i < threadCount; i++) {
            plusThreads[i].join();
            minusThreads[i].join();
        }

        System.out.println("최종 값 = " + sharedMap.get(KEY));
        long endTime = System.currentTimeMillis();
        System.out.println("실행 시간(ms) : " + (endTime-startTime)+"ms");
    }

    static void raceCondition(int num) {
        lock.lock();
        try {
            Integer value = sharedMap.getOrDefault(KEY, 0);
            value = value + num;

            // 수정 시간을 늦춰, 경합 확률 증가
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }

            sharedMap.put(KEY, value);
        }finally {
            lock.unlock();  //ReentrantLock 을 쓸시엔, unlock 체크 필수
        }
    }
}
