package com.example.chapter11.domain.model;

import com.example.chapter11.algorithm.*;

public class SchedulerFactory {
  public static Scheduler create(SchedulerType type, int timeSlice){
    return switch (type){
      case FCFS -> new FCFSScheduler();
      case SJF -> new SJFScheduler();
      case SRT -> new SRTScheduler();
      case RR -> new RRScheduler(timeSlice);
      default -> throw new IllegalArgumentException("Unsupported");
    };
  }
}
