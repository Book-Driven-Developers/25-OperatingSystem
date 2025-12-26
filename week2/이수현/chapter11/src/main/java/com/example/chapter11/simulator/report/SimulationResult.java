package com.example.chapter11.simulator.report;

import com.example.chapter11.domain.model.Process;
import lombok.Getter;

import java.util.List;

@Getter
public class SimulationResult {
  private final List<Process> finishedProcess;

  public SimulationResult(List<Process> finishedProcess){
    this.finishedProcess = finishedProcess;
  }

  public double averageWaitingTime(){
    return finishedProcess.stream()
            .mapToInt(Process::waiting)
            .average()
            .orElse(0.0);
  }

  public double averageTurnAroundTime(){
    return finishedProcess.stream()
            .mapToInt(Process::turnAround)
            .average()
            .orElse(0.0);
  }

  public double averageResponseTime(){
    return finishedProcess.stream()
            .mapToInt(Process::response)
            .average()
            .orElse(0.0);
  }
}
