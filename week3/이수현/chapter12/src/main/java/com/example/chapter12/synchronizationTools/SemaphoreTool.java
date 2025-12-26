package com.example.chapter12.synchronizationTools;

import java.util.concurrent.Semaphore;

public class SemaphoreTool implements SynchronizationTool{
  public final Semaphore semaphore = new Semaphore(1, true);
  private long balance = 0;

  @Override
  public void deposit(long amount) {
    acquire();
    try{
      validateAmount(amount);
      balance += amount;
    }finally {
      semaphore.release();
    }
  }

  @Override
  public void withdraw(long amount) throws InterruptedException {
    acquire();
    try{
      validateAmount(amount);
      if(balance < amount){
        throw new IllegalStateException("잔액 부족: 잔액은 " + balance + "원이고 출금 금액은 " + amount + "원 입니다.");
      }
      balance -= amount;
    }finally {
      semaphore.release();
    }
  }

  @Override
  public long getBalance() {
    acquire();
    try {
      return balance;
    } finally {
      semaphore.release();
    }
  }

  @Override
  public String name() {
    return "SEMAPHORE(1)";
  }

  private void acquire() {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("세마포어 얻는 과정 중 인터럽트 발생", e);
    }
  }

  private void validateAmount(long amount) {
    if (amount <= 0) throw new IllegalArgumentException("입력 오류: 입금 금액은 양수여야 합니다");
  }
}
