package com.example.chapter11.simulator.report;

import com.example.chapter11.domain.model.Process;

import java.util.Comparator;

public class ReportPrinter {
  public void print(SimulationResult result){
    System.out.println("\n===== Report =====");
    System.out.println("PID | ARR | BURST | START | FINISH | WAIT | TAT | RESP");

    result.getFinishedProcess().stream()
            .sorted(Comparator.comparingLong(Process::getPid))
            .forEach(p -> System.out.printf(
                    "%3d | %3d | %5d | %5d | %6d | %4d | %3d | %4d%n",
                    p.getPid(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getStartTime(),
                    p.getFinishTime(),
                    p.waiting(),
                    p.turnAround(),
                    p.response()
            ));

    System.out.printf("AVG WAIT=%.2f, AVG TAT=%.2f, AVG RESP=%.2f%n",
            result.averageWaitingTime(),
            result.averageTurnAroundTime(),
            result.averageResponseTime()
    );
    System.out.println("========================\n");
  }
}
