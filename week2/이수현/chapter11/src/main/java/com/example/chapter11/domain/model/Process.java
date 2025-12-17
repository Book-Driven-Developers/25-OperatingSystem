package com.example.chapter11.domain.model;

import lombok.Getter;

// 프로세스의 PCB를 단순화한 모델
@Getter
public class Process {
  private final long pid;
  private final int arrivalTime;  //도착 시간
  private final int burstTime; // 실행시간
  private int remainingTime;

  // 실행 결과
  private Integer startTime;  //처음 CPU를 점유한 시간
  public Integer finishTime;  //프로세스 종료 시간

  public Process(long pid, int arrivalTime, int burstTime){
    this.pid = pid;
    this.arrivalTime = arrivalTime;
    this.burstTime = burstTime;
    this.remainingTime = burstTime;

  }

  // 맨 처음 실행 시작 시간 마킹
  public void markStarted(int clock){
    if (startTime == null) startTime = clock;
  }

  // 종료 시간 마킹
  public void markFinished(int clock){
    finishTime = clock;
  }

  // clock 한번 실행
  public void runTick(){
    if (remainingTime > 0) remainingTime--;
  }

  public boolean isFinished() {
    return remainingTime == 0;
  }

  // 전체 소요 시간
  public int turnAround(){
    return finishTime - arrivalTime;
  }

  // Ready queue에서 총 기다린 시간
  public int waiting(){
    return turnAround() - burstTime;
  }

  // 도착 후 첫 실행까지 걸린 시간
  public int response() {
    return startTime - arrivalTime;
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Process(").append(pid).append(") -> ")
            .append("arriveTime: ").append(arrivalTime)
            .append(", bustTime: ").append(burstTime)
            .append(", remainingTime:").append(remainingTime);
    return sb.toString();
  }
}
