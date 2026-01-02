package com.example.chapter13.strategy;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;
import com.example.chapter13.scenario.DeadlockReproducer;

// 타조 알고리즘 적용 모드
public class OstrichStrategy implements Strategy{
  private final boolean reproduce;

  public OstrichStrategy(boolean reproduce){
    this.reproduce = reproduce;
  }
  @Override
  public void play(ConsoleUI consoleUI, DeadlockScenario deadlockScenario) {
    if(!reproduce){
      consoleUI.println("타조 알고리즘: '문제 없겠지' 하고 그냥 넘어갑니다.");
      consoleUI.println("현실에서는 장애/확률/비용을 고려해 선택하는 전략입니다.");
      return;
    }

    consoleUI.println("교착 상태를 발생시킵니다.");
    DeadlockReproducer deadlockReproducer = new DeadlockReproducer();
    var result = deadlockReproducer.runDeadlock(deadlockScenario.lock1, deadlockScenario.lock2);
    consoleUI.println("스레드 상태를 관찰해보세요: ThreadA, ThreadB가 멈춰 있으면 교착 가능성이 큽니다.");
    consoleUI.println("참고: 이 데모는 의도적으로 프로그램이 '안 끝날 수' 있습니다. (Ctrl+C로 종료 가능)");
  }
}
