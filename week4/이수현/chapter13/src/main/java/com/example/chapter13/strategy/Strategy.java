package com.example.chapter13.strategy;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;

public interface Strategy {
  void play(ConsoleUI consoleUI, DeadlockScenario deadlockScenario);
}
