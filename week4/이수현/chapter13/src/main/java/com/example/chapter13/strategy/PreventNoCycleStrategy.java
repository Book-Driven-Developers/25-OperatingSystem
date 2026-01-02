package com.example.chapter13.strategy;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;
import com.example.chapter13.util.Sleep;

// 원향 대기 조건 없앰으로써, 교착 상태 예방 모드
public class PreventNoCycleStrategy implements Strategy{
  @Override
  public void play(ConsoleUI consoleUI, DeadlockScenario deadlockScenario) {
    consoleUI.println("예방(원형대기 제거): 모든 스레드가 lock1 -> lock2 순서로만 락을 잡게 합니다.");

    Thread threadA = new Thread(() -> work(consoleUI, deadlockScenario, "ThreadA"), "ThreadA");
    Thread threadB = new Thread(() -> work(consoleUI, deadlockScenario, "ThreadB"), "ThreadB");

    threadA.start();
    threadB.start();

    join(consoleUI, threadA);
    join(consoleUI, threadB);

    consoleUI.println("완료! 동일 순서 획득으로 원형대기가 생기지 않아 교착이 예방됩니다.");
  }

  private void work(ConsoleUI consoleUI, DeadlockScenario deadlockScenario, String threadName) {
    deadlockScenario.lock1.lock();
    try {
      consoleUI.println(threadName + " acquired lock1");
      Sleep.ms(100);
      deadlockScenario.lock2.lock();
      try {
        consoleUI.println(threadName + " acquired lock2");
        Sleep.ms(200);
      } finally { deadlockScenario.lock2.unlock(); }
    } finally { deadlockScenario.lock1.unlock(); }
  }

  private void join(ConsoleUI ui, Thread t) {
    try { t.join(2000); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    if (t.isAlive()) ui.println(t.getName() + " still alive (unexpected here)");
  }
}
