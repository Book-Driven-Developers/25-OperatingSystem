package ch12;

import ch12.sync.*;

import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) throws Exception {
        List<SyncStrategy> strategies = List.of(
                new NoStrategy(),
                new MutexStrategy(),
                new SemaphoreStrategy(1, true),
                new MonitorStrategy()
        );

        int threads = 10;
        int incrementsPerThread = 1000;
        int expected = threads * incrementsPerThread;

        for (SyncStrategy strategy : strategies) {
            runAndPrint(
                    strategy.getClass().getSimpleName(),
                    strategy,
                    threads,
                    incrementsPerThread,
                    expected
            );
        }
    }

    private static void runAndPrint(String name, SyncStrategy strategy, int threads, int incrementsPerThread, int expected) throws InterruptedException {
        Counter counter = new Counter(strategy);
        List<Thread> list = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            Thread th = new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    counter.increment();
                }
            });
            list.add(th);
        }

        for (Thread th : list) {
            th.start();
        }
        for (Thread th : list) {
            th.join();
        }

        int actual = counter.get();
        System.out.println(
                String.format("[%s]\n기댓값=%d, 현재값=%d", name, expected, actual)
        );
    }
}
