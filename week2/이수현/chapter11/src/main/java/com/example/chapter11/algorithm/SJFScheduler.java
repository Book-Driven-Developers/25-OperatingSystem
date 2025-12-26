package com.example.chapter11.algorithm;

import com.example.chapter11.domain.model.Process;

import java.util.Queue;

public class SJFScheduler implements Scheduler{
  @Override
  public Process pickNext(Queue<Process> readyQueue, Process current) {
    if (current != null) return current;  //비선점

    Process best = null;
    for(Process process : readyQueue){
      if(best == null || process.getBurstTime() < best.getBurstTime()) {
        best = process;
      }
    }
    if(best != null) readyQueue.remove(best);
    return best;
  }
}
