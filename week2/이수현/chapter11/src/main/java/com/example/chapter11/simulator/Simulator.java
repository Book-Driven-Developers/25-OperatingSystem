package com.example.chapter11.simulator;

import com.example.chapter11.algorithm.Scheduler;
import com.example.chapter11.domain.model.Process;
import com.example.chapter11.simulator.report.SimulationResult;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// 최소 2개의 스레드가 동시에 돌아간다.
// 시뮬레이터 스레드 + 콘솔 입력 리스너 스레드
@Getter
public class Simulator {
  private final BlockingQueue<Process> readyQueue = new LinkedBlockingQueue<>(); // 동시 접근이 발생하므로 thread-safe 컬렉션 사용
  private final List<Process> futureArrivals; //준비 큐에 넣기 전 arrivalTime에 따른 임시 리스트
  private final List<Process> finishsed = new ArrayList<>();
  private final Scheduler scheduler;

  private int clock = 0;
  private Process current = null;

  public Simulator(List<Process> initialProcesses, Scheduler scheduler){
    this.futureArrivals = new ArrayList<>(initialProcesses);
    this.futureArrivals.sort(Comparator.comparingInt(Process::getArrivalTime));
    this.scheduler = scheduler;
  }

  public void tick(){
    moveArrivedProcess();

    current = scheduler.pickNext(readyQueue, current);
    if(current != null){
      current.markStarted(clock);
      current.runTick();

      if(current.isFinished()){
        current.markFinished(clock + 1);
        finishsed.add(current);
        current = null;
      }
    }
    clock++;
  }

  public boolean isFinished(){
    synchronized (futureArrivals){
      return futureArrivals.isEmpty() && readyQueue.isEmpty() && current==null;
    }
  }

  public SimulationResult getResult(){
    return new SimulationResult(List.copyOf(finishsed));
  }

  private void moveArrivedProcess(){
    synchronized (futureArrivals){
      while (!futureArrivals.isEmpty()
              && futureArrivals.getFirst().getArrivalTime() <= clock) {
        readyQueue.offer(futureArrivals.removeFirst());
      }
    }
  }
}
