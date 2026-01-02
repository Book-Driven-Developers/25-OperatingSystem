package com.example.chapter13.strategy.banker;

import com.example.chapter13.console.ConsoleUI;
import com.example.chapter13.scenario.DeadlockScenario;
import com.example.chapter13.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;

// 은행원 알고리즘 적용 모드
public class BankerStrategy implements Strategy {
  @Override
  public void play(ConsoleUI consoleUI, DeadlockScenario deadlockScenario) {
    consoleUI.println("은행원 알고리즘(회피) 데모: 자원 요청을 '안전 상태'일 때만 승인합니다.");
    consoleUI.println("여기서는 Lock 교착 대신, 자원(2개) 할당 시뮬레이션으로 진행합니다.");

    // 프로세스 2개(P0,P1), 자원 2개(R1,R2)
    // 전체 자원: R1=1, R2=1 (락 2개 느낌)
    // available = (1,1)
    // max:
    //  P0: (1,1) / P1: (1,1)
    // alloc: 초기 0
    BankerState state = new BankerState(
            new int[]{1, 1},
            new int[][]{{1,1},{1,1}},
            new int[][]{{0,0},{0,0}}
    );

    consoleUI.println("초기 상태: available=(1,1), P0/P1 max=(1,1)");
    // 사용자가 “P0가 R1 요청”, “P1이 R2 요청” 이런 식으로 진행하는 게임처럼
    consoleUI.println("요청 시나리오를 자동으로 한 번 돌려볼게요:");
    request(consoleUI, state, 0, new int[]{1,0}); // P0: R1 요청
    request(consoleUI, state, 1, new int[]{0,1}); // P1: R2 요청
    request(consoleUI, state, 0, new int[]{0,1}); // P0: R2 요청
    request(consoleUI, state, 1, new int[]{1,0}); // P1: R1 요청

    consoleUI.println("포인트: '승인하면 unsafe'면 거절(대기)해서 교착을 회피합니다.");
  }

  private void request(ConsoleUI consoleUI, BankerState bankerState, int process, int[] requires) {
    consoleUI.println("\nRequest: Process" + process + " requests (" + requires[0] + "," + requires[1] + ")");
    if (!canRequest(bankerState, process, requires)) {
      consoleUI.println(" -> 거절(요청이 need/available을 초과)");
      return;
    }

    BankerState trial = bankerState.copy();
    // 가상 할당
    for (int r = 0; r < requires.length; r++) {
      trial.available[r] -= requires[r];
      trial.alloc[process][r] += requires[r];
    }

    if (isSafe(trial)) {
      // 실제 반영
      for (int r = 0; r < requires.length; r++) {
        bankerState.available[r] -= requires[r];
        bankerState.alloc[process][r] += requires[r];
      }
      consoleUI.println(" -> 승인 (safe)");
      consoleUI.println("    now available=(" + bankerState.available[0] + "," + bankerState.available[1] + ")");
    } else {
      consoleUI.println(" -> 보류 (승인하면 unsafe -> 교착 위험)");
    }
  }

  private boolean canRequest(BankerState bankerState, int process, int[] requires) {
    int[][] need = bankerState.need();
    for (int r = 0; r < requires.length; r++) {
      if (requires[r] > need[process][r]) return false;
      if (requires[r] > bankerState.available[r]) return false;
    }
    return true;
  }

  // 안전성 검사: 안전 순서가 존재하면 safe
  private boolean isSafe(BankerState bankerState) {
    int pN = bankerState.max.length;
    int rN = bankerState.available.length;
    int[] work = java.util.Arrays.copyOf(bankerState.available, rN);
    boolean[] finish = new boolean[pN];
    int[][] need = bankerState.need();

    List<Integer> seq = new ArrayList<>();
    boolean progressed;
    do {
      progressed = false;
      for (int p = 0; p < pN; p++) {
        if (finish[p]) continue;
        boolean ok = true;
        for (int r = 0; r < rN; r++) if (need[p][r] > work[r]) { ok = false; break; }
        if (ok) {
          for (int r = 0; r < rN; r++) work[r] += bankerState.alloc[p][r];
          finish[p] = true;
          seq.add(p);
          progressed = true;
        }
      }
    } while (progressed);

    for (boolean f : finish) if (!f) return false;
    return true;
  }
}
