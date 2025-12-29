package com.example.chapter12.synchronizationTools;

public class MonitorTool implements SynchronizationTool{
  private long balance = 0;

  @Override
  public synchronized void deposit(long amount) {
    validateAmount(amount);
    balance += amount;
    notifyAll();  // 대기중이었던 출금 스레드 깨움
  }

  @Override
  public synchronized void withdraw(long amount) throws InterruptedException {
    validateAmount(amount);
    while(balance < amount){
      wait(); // 잔액이 찰 때까지 대기
    }
    balance -= amount;
  }

  @Override
  public synchronized long getBalance() {
    return balance;
  }

  @Override
  public String name() {
    return "";
  }

  private void validateAmount(long amount) {
    if (amount <= 0) throw new IllegalArgumentException("입력 오류: 입금 금액은 양수여야 합니다");
  }
}
