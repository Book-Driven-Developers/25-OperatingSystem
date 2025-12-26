package com.example.chapter12.synchronizationTools;

public interface SynchronizationTool {
  void deposit(long amount);
  void withdraw(long amount) throws InterruptedException;
  long getBalance();
  String name();
}
