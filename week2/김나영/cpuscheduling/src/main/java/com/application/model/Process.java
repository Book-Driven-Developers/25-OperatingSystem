package com.application.model;

public class Process {
    int pid; // 프로세스 ID
    int burstTime; // CPU 실행 시간
    int arrivalTime; // 도착 시간
    int remainingTime; // 남은 실행 시간
    int completionTime; // 완료 시간

    public Process(int pid, int burstTime, int arrivalTime) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.remainingTime = burstTime;
    }

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getTurnaroundTime() {
        return completionTime - arrivalTime;
    }
}
