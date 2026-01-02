package com.example.chapter13.game;

public enum Mode {
  REPRODUCE_DEADLOCK,      // 교착 재현
  PREVENTION_NO_CYCLE,     // 예방: 원형대기 제거(락 순서 고정)
  BANKER_AVOIDANCE,        // 회피: 은행원 알고리즘(단순 시뮬)
  DETECT_AND_RECOVER,      // 검출 후 회복(감시 + 중단/타임아웃)
  OSTRICH_IGNORE           // 타조: 무시
}
