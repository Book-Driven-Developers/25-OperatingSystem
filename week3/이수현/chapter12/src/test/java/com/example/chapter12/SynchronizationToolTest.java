package com.example.chapter12;

import com.example.chapter12.synchronizationTools.SynchronizationTool;
import com.example.chapter12.synchronizationTools.ToolType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizationToolTest {

  @ParameterizedTest(name = "{0} - 단일 스레드 입출금")
  @EnumSource(ToolType.class)
  @DisplayName("단일 스레드 입금/출금")
  void singleThread_basic(ToolType type) throws InterruptedException {
    SynchronizationTool account = type.create();

    account.deposit(1000);
    account.withdraw(300);

    assertEquals(700, account.getBalance());
  }

  @ParameterizedTest(name = "{0} - 동시 입금")
  @EnumSource(ToolType.class)
  @DisplayName("여러 스레드가 동시에 입금해도 정확하다")
  void concurrentDeposits(ToolType type) throws Exception {
    SynchronizationTool account = type.create();

    int threads = 50;
    int ops = 2000;

    runConcurrently(threads, () -> {
      for (int i = 0; i < ops; i++) {
        account.deposit(1);
      }
    });

    assertEquals(threads * ops, account.getBalance());
  }

  @ParameterizedTest(name = "{0} - 동시 입출금 혼합")
  @EnumSource(ToolType.class)
  @DisplayName("동시 입출금 혼합 상황에서도 잔액이 일관적이다")
  void concurrentDepositWithdraw(ToolType type) throws Exception {
    SynchronizationTool account = type.create();
    account.deposit(1_000_000);

    int threads = 40;
    int ops = 5000;

    runConcurrently(threads, () -> {
      for (int i = 0; i < ops; i++) {
        account.deposit(2);
        try {
          account.withdraw(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        }
      }
    });

    long expected = 1_000_000 + (long) threads * ops;
    assertEquals(expected, account.getBalance());
  }

  @ParameterizedTest(name = "{0} - 출금 대기 중 인터럽트")
  @EnumSource(ToolType.class)
  @DisplayName("출금 대기 중 interrupt 되면 InterruptedException 발생")
  void withdrawInterrupted(ToolType type) throws Exception {
    SynchronizationTool account = type.create();

    Thread t = new Thread(() -> {
      try {
        account.withdraw(100); // 잔액 0 → 대기
        fail("InterruptedException이 발생해야 합니다.");
      } catch (InterruptedException e) {
        // 정상
      }
    });

    t.start();
    Thread.sleep(100); // withdraw 진입 보장
    t.interrupt();

    t.join(1000);
    assertFalse(t.isAlive());
  }

  private void runConcurrently(int threadCount, Runnable job) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(threadCount);
    CountDownLatch ready = new CountDownLatch(threadCount);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(threadCount);

    List<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      futures.add(pool.submit(() -> {
        ready.countDown();
        try {
          start.await();
          job.run();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException(e);
        } finally {
          done.countDown();
        }
      }));
    }

    ready.await();
    start.countDown();
    done.await();

    for (Future<?> f : futures) {
      f.get();
    }

    pool.shutdownNow();
  }
}
