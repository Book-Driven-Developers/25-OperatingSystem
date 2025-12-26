package com.example.chapter12.synchronizationTools;

import java.util.concurrent.locks.ReentrantLock;

public class MutexTool implements SynchronizationTool {
  private final ReentrantLock reentrantLock = new ReentrantLock(true);
  //만약 읽기 연산(잔액 확인)이 많을 경우, 읽기와 쓰기 락을 분리해줘야할 필요가 있다.
  //이때는 ReadWriteLock rwLock = new ReentrantReadWriteLock();을 사용하는 것이 좋다.
  private long balance = 0;

  @Override
  public void deposit(long amount) {
    reentrantLock.lock();
    try{
      validateAmount(amount);
      balance += amount;
    }finally {
      reentrantLock.unlock();  // 임계구역이 끝나거나 도중에 에러가 발생해도, 락은 해제해야함. (점유 방지)
    }
  }

  @Override
  public void withdraw(long amount) throws InterruptedException {
    reentrantLock.lock();
    try{
      validateAmount(amount);
      if(balance < amount){
        throw new IllegalStateException("잔액 부족: 잔액은 " + balance + "원이고 출금 금액은 " + amount + "원 입니다.");
      }
      balance -= amount;
    }finally {
      reentrantLock.unlock();
    }
  }

  @Override
  public long getBalance() { // 읽기 스레드 (최신값 보장을 위해 락을 걸어둠)
    reentrantLock.lock();
    try{
      return balance;
    }finally {
      reentrantLock.unlock();
    }
  }

  @Override
  public String name() {
    return "MUTEX(ReentrantLock)";
  }

  private void validateAmount(long amount) {
    if (amount <= 0) throw new IllegalArgumentException("입력 오류: 입금 금액은 양수여야 합니다");
  }
}
