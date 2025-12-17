package com.application.controller;

import com.application.scheduler.RoundRobinScheduler;
import com.application.model.Process;

import java.util.ArrayList;
import java.util.List;

public class ScheduleController {
    private List<Process> processes;
    private int timeQuantum;

    public ScheduleController(int timeQuantum) {
        this.processes = new ArrayList<>();
        this.timeQuantum = timeQuantum;
    }

    public void addProcess(int pid, int burstTime, int arrivalTime) {
        processes.add(new Process(pid, burstTime, arrivalTime));
    }

    public void run() {
        if (processes.isEmpty()) {
            System.out.println("실행할 프로세스가 없습니다.");
            return;
        }

        RoundRobinScheduler scheduler = new RoundRobinScheduler(timeQuantum);
        scheduler.schedule(processes);
    }
}
