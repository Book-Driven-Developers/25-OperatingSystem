package com.example.chapter11.domain.model;

import com.example.chapter11.algorithm.FCFSScheduler;
import com.example.chapter11.algorithm.SJFScheduler;
import com.example.chapter11.algorithm.SRTScheduler;
import com.example.chapter11.algorithm.Scheduler;

public class SchedulerFactory {
  public static Scheduler create(SchedulerType type){
    return switch (type){
      case FCFS -> new FCFSScheduler();
      case SJF -> new SJFScheduler();
      case SRT -> new SRTScheduler();
      default -> new FCFSScheduler();
    };
  }
}
