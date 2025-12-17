package com.example.chapter11.view.console;

import com.example.chapter11.domain.model.Process;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ProcessArrivalHandler {
  private final BlockingQueue<Process> readyQueue;
  private final List<Process> futureArrivals;

  public ProcessArrivalHandler(BlockingQueue<Process> readyQueue, List<Process> futureArrivals){
    this.readyQueue = readyQueue;
    this.futureArrivals = futureArrivals;
  }

  public void handle(Process process, int currentClock){
    if(process.getArrivalTime() <= currentClock){
      readyQueue.offer(process);
    }else{
      synchronized (futureArrivals){
        futureArrivals.add(process);
        futureArrivals.sort(Comparator.comparingInt(Process::getArrivalTime));
      }
    }
  }
}
