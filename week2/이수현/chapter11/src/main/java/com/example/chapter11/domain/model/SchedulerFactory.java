package com.example.chapter11.domain.model;

import com.example.chapter11.algorithm.FCFSScheduler;
import com.example.chapter11.algorithm.Scheduler;

public class SchedulerFactory {
  public static Scheduler create(SchedulerType type){
    return switch (type){
      case FCFS -> new FCFSScheduler();
      default -> new FCFSScheduler();
    };
  }
}
