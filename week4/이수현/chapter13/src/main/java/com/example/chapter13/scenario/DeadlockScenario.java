package com.example.chapter13.scenario;

import java.util.concurrent.locks.ReentrantLock;

//Deadlock 재현 모드
// 공유 자원 2개, 스레드 2개
// Thread-A는 L1→L2, Thread-B는 L2→L1 순서로 락을 잡게 해서 원형대기를 만들기
public class DeadlockScenario {
  // 재현용 락 2개
  public final ReentrantLock lock1 = new ReentrantLock();
  public final ReentrantLock lock2 = new ReentrantLock();
}
