package operation_system.cpu_scheduling.thread;

import java.util.HashMap;
import java.util.Map;

//문제상황 : 공유 자원에 대한 동시성 문제 발생
public class raceConditionMain {

    static Map<String, Integer> sharedMap = new HashMap<>();
    static final String KEY = "count";

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
        Integer value = sharedMap.getOrDefault(KEY, 0);
        value = value + num;

        // 수정 시간을 늦춰, 경합 확률 증가
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        sharedMap.put(KEY, value);
    }
}
