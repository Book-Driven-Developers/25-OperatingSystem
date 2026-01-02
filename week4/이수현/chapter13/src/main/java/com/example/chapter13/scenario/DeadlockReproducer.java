package com.example.chapter13.scenario;

import com.example.chapter13.util.Sleep;
import org.apache.commons.logging.impl.Slf4jLogFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockReproducer {
  public record RunResult(Thread t1, Thread t2) {}

  public RunResult runDeadlock(ReentrantLock lock1, ReentrantLock lock2){
    CountDownLatch ready = new CountDownLatch(2);

    Thread threadA = new Thread(() -> {
      ready.countDown();
      Sleep.ms(50);
      lock1.lock();
      try{
        Sleep.ms(200); // 상대가 다른 락 잡을 시간 벌어주기
        lock2.lock();  //교착 발생 가능
        try { Sleep.ms(200);}
        finally { lock2.unlock(); }
      }finally {
        lock1.unlock();
      }
    }, "ThreadA");

    Thread threadB = new Thread(() -> {
      ready.countDown();
      Sleep.ms(50);
      lock2.lock();
      try{
        Sleep.ms(200);
        lock1.lock();  //교착 가능
        try { Sleep.ms(200); }
        finally { lock1.unlock(); }
      } finally {
        lock2.unlock();
      }
    }, "ThreadB");

    threadA.start();
    threadB.start();
    return new RunResult(threadA, threadB);
  }
}
