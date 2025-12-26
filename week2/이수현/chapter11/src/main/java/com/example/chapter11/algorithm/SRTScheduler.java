package com.example.chapter11.algorithm;

import com.example.chapter11.domain.model.Process;

import java.util.Queue;

public class SRTScheduler implements Scheduler{
  @Override
  public Process pickNext(Queue<Process> readyQueue, Process current) {
    if (current != null) {
      readyQueue.offer(current); //선점 -> current도 후보에 포함시킨다.
    }

    Process best = null;
    for(Process process : readyQueue){
      //남은 실행 시간이 더 짧은 것이 선점
      if (best == null || process.getRemainingTime() < best.getRemainingTime()){
        best = process;
      }
    }
    if (best != null) {
      readyQueue.remove(best);
    }
    return best;
  }
}
