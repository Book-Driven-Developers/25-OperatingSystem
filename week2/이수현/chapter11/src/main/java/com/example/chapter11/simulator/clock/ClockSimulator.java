package com.example.chapter11.simulator.clock;

import java.util.function.Supplier;

public class ClockSimulator implements ClockProvider {
  private final Supplier<Integer> clockSupplier;

  public ClockSimulator(Supplier<Integer> clockSupplier){
    this.clockSupplier = clockSupplier;
  }
  @Override
  public int now() {
    return clockSupplier.get();
  }
}
