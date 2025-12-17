package com.example.chapter11.algorithm;

import com.example.chapter11.domain.model.Process;
import java.util.Queue;

public class FCFSScheduler implements Scheduler {
  @Override
  public Process pickNext(Queue<Process> readyQueue, Process current) {
    if(current != null) return current;  // 비선점: 실행중이면 계속 실행
    return readyQueue.poll();  // 다음 프로세스 실행
  }
}
