package com.example.chapter13.game;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;
import com.example.chapter13.strategy.DetectRecoveryStrategy;
import com.example.chapter13.strategy.OstrichStrategy;
import com.example.chapter13.strategy.PreventNoCycleStrategy;
import com.example.chapter13.strategy.Strategy;
import com.example.chapter13.strategy.banker.BankerStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GameRunner {
  private final ConsoleUI consoleUI = new ConsoleUI();
  private final DeadlockScenario deadlockScenario = new DeadlockScenario();
  private final Map<Mode, Strategy> strategies = Map.of(
          Mode.REPRODUCE_DEADLOCK, new OstrichStrategy(true),          // 일부러 교착 재현(=무시)
          Mode.PREVENTION_NO_CYCLE, new PreventNoCycleStrategy(),
          Mode.BANKER_AVOIDANCE, new BankerStrategy(),
          Mode.DETECT_AND_RECOVER, new DetectRecoveryStrategy(),
          Mode.OSTRICH_IGNORE, new OstrichStrategy(false)
  );

  public void start() {
    consoleUI.println("=== Deadlock Game ===");
    while (true) {
      consoleUI.line();
      consoleUI.println("1) 교착 상태 재현");
      consoleUI.println("2) 예방: 원형대기 제거(락 획득 순서 고정)");
      consoleUI.println("3) 회피: 은행원 알고리즘(시뮬레이션)");
      consoleUI.println("4) 검출 후 회복(감시 + 중단/타임아웃)");
      consoleUI.println("5) 타조 알고리즘(무시)");
      consoleUI.println("0) 종료");
      int select = consoleUI.askInt("선택 >", 0, 5);
      if (select == 0) break;

      Mode mode = switch (select) {
        case 1 -> Mode.REPRODUCE_DEADLOCK;
        case 2 -> Mode.PREVENTION_NO_CYCLE;
        case 3 -> Mode.BANKER_AVOIDANCE;
        case 4 -> Mode.DETECT_AND_RECOVER;
        case 5 -> Mode.OSTRICH_IGNORE;
        default -> throw new IllegalStateException();
      };

      consoleUI.line();
      consoleUI.println("[MODE] " + mode);
      strategies.get(mode).play(consoleUI, deadlockScenario);
      consoleUI.println("\n(엔터를 누르면 메뉴로 돌아갑니다)");
      consoleUI.ask("");
    }
    consoleUI.println("Bye!");
  }
}
