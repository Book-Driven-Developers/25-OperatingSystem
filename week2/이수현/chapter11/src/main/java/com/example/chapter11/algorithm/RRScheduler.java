package com.example.chapter11.algorithm;

import com.example.chapter11.domain.model.Process;

import java.util.Queue;

public class RRScheduler implements Scheduler{
  private final int timeSlice;
  private int timeSliceUsed = 0;
  private long currentPid = -1;

  public RRScheduler(int timeSlice){
    this.timeSlice = timeSlice;
  }

  @Override
  public Process pickNext(Queue<Process> readyQueue, Process current) {
    //새로운 프로세스일 때 초기화
    if(current == null){
      return pollNext(readyQueue);
    }

    // 프로세스가 바뀌었으면 slice 리셋
    if (current.getPid() != currentPid) resetSlice(current);

    // timeSlice 끝났을 경우, 다음 프로세스가 선점
    if (timeSliceUsed >= timeSlice){
      readyQueue.offer(current);
      return pollNext(readyQueue);
    }

    // timeSlice 안 끝났을 경우, 현재 프로세스가 계속 점유
    timeSliceUsed++;
    return current;
  }

  private void resetSlice(Process process){
   timeSliceUsed = 0;
   currentPid = (process == null) ? -1 : process.getPid();
  }

  private Process pollNext(Queue<Process> readyQueue){
    Process next = readyQueue.poll();
    resetSlice(next);
    return next;
  }
}
