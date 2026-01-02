package com.example.chapter13.strategy;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;
import com.example.chapter13.util.Sleep;

import java.util.concurrent.TimeUnit;

// 교착 상태 검출 및 회복 모드
public class DetectRecoveryStrategy implements Strategy {
  @Override
  public void play(ConsoleUI consoleUI, DeadlockScenario deadlockScenario) {
    consoleUI.println("검출 후 회복: lock2 획득을 tryLock(timeout)으로 시도하고, 실패하면 회복(롤백/포기/재시도)합니다.");

    Thread threadA = new Thread(() -> worker(consoleUI, deadlockScenario, true), "T-A");
    Thread threadB = new Thread(() -> worker(consoleUI, deadlockScenario, false), "T-B");

    threadA.start();
    threadB.start();
    join(consoleUI, threadA);
    join(consoleUI, threadB);

    consoleUI.println("완료! '교착이 될 상황'을 감지하고 한 쪽이 물러나 회복하도록 만든 방식입니다.");
  }

  // A는 lock1->lock2, B는 lock2->lock1 순서로 "교착 시도"를 하되
  // 두 번째 락은 tryLock(timeout)으로 감지/회복
  private void worker(ConsoleUI consoleUI, DeadlockScenario deadlockScenario, boolean aOrder) {
    var first = aOrder ? deadlockScenario.lock1 : deadlockScenario.lock2;
    var second = aOrder ? deadlockScenario.lock2 : deadlockScenario.lock1;

    for (int attempt = 1; attempt <= 5; attempt++) {
      first.lock();
      try {
        consoleUI.println(Thread.currentThread().getName() + " attempt " + attempt + " acquired FIRST");
        Sleep.ms(150);

        boolean gotSecond = secondTryLock(consoleUI, second, 300);
        if (!gotSecond) {
          consoleUI.println(Thread.currentThread().getName() + " detected potential deadlock -> recover (release FIRST, backoff)");
          // 회복(데모): 첫 락 풀고 backoff 후 재시도
          Sleep.ms(100 + (long)(Math.random() * 150));
          continue;
        }

        try {
          consoleUI.println(Thread.currentThread().getName() + " acquired SECOND -> critical section");
          Sleep.ms(200);
          return; // 성공
        } finally {
          second.unlock();
        }
      } finally {
        first.unlock();
      }
    }

    consoleUI.println(Thread.currentThread().getName() + " failed after retries (give up)");
  }

  private boolean secondTryLock(ConsoleUI ui, java.util.concurrent.locks.ReentrantLock lock, long timeoutMs) {
    try {
      return lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private void join(ConsoleUI ui, Thread t) {
    try { t.join(4000); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    if (t.isAlive()) ui.println(t.getName() + " still alive (unexpected in this demo)");
  }
}
