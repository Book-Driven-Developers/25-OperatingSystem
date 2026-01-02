package com.example.chapter13.util;

public class Sleep {
  public static void ms(long ms) {
    try { Thread.sleep(ms); }
    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
  }
}
