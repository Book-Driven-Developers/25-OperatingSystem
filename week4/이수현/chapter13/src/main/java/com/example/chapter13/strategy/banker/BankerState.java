package com.example.chapter13.strategy.banker;

import java.util.Arrays;

public class BankerState {
  // 자원 타입 2개(예: R1, R2)만 데모로
  // available: 가용 자원
  // max[proc][res], alloc[proc][res]
  public int[] available;
  public int[][] max;
  public int[][] alloc;

  public BankerState(int[] available, int[][] max, int[][] alloc) {
    this.available = available;
    this.max = max;
    this.alloc = alloc;
  }

  public int[][] need() {
    int p = max.length, r = available.length;
    int[][] need = new int[p][r];
    for (int i = 0; i < p; i++) {
      for (int j = 0; j < r; j++) need[i][j] = max[i][j] - alloc[i][j];
    }
    return need;
  }

  public BankerState copy() {
    return new BankerState(
            Arrays.copyOf(available, available.length),
            deepCopy(max),
            deepCopy(alloc)
    );
  }

  private static int[][] deepCopy(int[][] a) {
    int[][] b = new int[a.length][];
    for (int i = 0; i < a.length; i++) b[i] = Arrays.copyOf(a[i], a[i].length);
    return b;
  }
}
