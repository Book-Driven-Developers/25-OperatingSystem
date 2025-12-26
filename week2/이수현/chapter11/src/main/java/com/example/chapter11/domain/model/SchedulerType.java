package com.example.chapter11.domain.model;

public enum SchedulerType {
  FCFS,
  SJF,
  RR,
  SRT,
  PRIORITY,
  MQ,
  MFQ;

  public static SchedulerType from(String schedulerName){
    try{
      return SchedulerType.valueOf(schedulerName.toUpperCase());
    }catch (IllegalArgumentException iae){
      throw new IllegalArgumentException(
              "지원하지 않는 스케줄링 알고리즘입니다: " + schedulerName
                      + " (가능: FCFS, SJF, RR, SRT, PRIORITY, MQ, MFQ)"
      );
    }
  }
}
