package com.application.scheduler;

import java.util.*;
import com.application.model.Process;

public class RoundRobinScheduler{
    private int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    public void schedule(List<Process> processes) {
        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> arrivalList = new ArrayList<>(processes);

        // 도착 시간 순서로 정렬
        arrivalList.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        int index = 0;

        System.out.println("\n=== Round Robin Scheduling (Time Quantum: " + timeQuantum + ") ===\n");

        if (index < arrivalList.size()) {
            currentTime = arrivalList.get(0).arrivalTime;
            readyQueue.add(arrivalList.get(index++));
        }

        while (!readyQueue.isEmpty() || index < arrivalList.size()) {

            // 현재 시간까지 도착한 프로세스들을 큐에 추가
            while (index < arrivalList.size() &&
                    arrivalList.get(index).arrivalTime <= currentTime) {
                readyQueue.add(arrivalList.get(index++));
            }

            // 큐가 비어있으면 다음 프로세스 도착 시간으로 점프
            if (readyQueue.isEmpty()) {
                currentTime = arrivalList.get(index).arrivalTime;
                readyQueue.add(arrivalList.get(index++));
                continue;
            }

            Process current = readyQueue.poll();
            int execTime = Math.min(timeQuantum, current.remainingTime);

            // 실행 중인 프로세스 출력
            System.out.printf("[Time %d-%d] P%d 실행 중 (남은 시간: %d -> %d)\n",
                    currentTime, currentTime + execTime, current.pid,
                    current.remainingTime, current.remainingTime - execTime);

            // 프로세스 실행
            currentTime += execTime;
            current.remainingTime -= execTime;

            // 실행 중에 도착한 프로세스들 큐에 추가
            while (index < arrivalList.size() &&
                    arrivalList.get(index).arrivalTime <= currentTime) {
                readyQueue.add(arrivalList.get(index++));
            }

            // 프로세스 완료 여부 확인
            if (current.remainingTime > 0) {
                readyQueue.add(current);
            } else {
                current.completionTime = currentTime;
                // 완료된 프로세스 출력
                System.out.printf(">>> P%d 완료! (완료 시간: %d, 반환 시간: %d, 대기 시간: %d)\n\n",
                        current.pid, current.completionTime,
                        current.getTurnaroundTime(), current.getWaitingTime());
            }
        }

        // 최종 결과 출력
        printFinalResults(processes);

    }

    private void printFinalResults(List<Process> processes) {
        System.out.println("=".repeat(70));
        System.out.println("=== 최종 결과 ===\n");
        System.out.println("PID\tArrival\tBurst\tCompletion\tTurnaround\tWaiting");
        System.out.println("---\t-------\t-----\t----------\t----------\t-------");

        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;

        for (Process p : processes) {
            System.out.printf("P%d\t%d\t%d\t%d\t\t%d\t\t%d\n",
                    p.pid, p.arrivalTime, p.burstTime,
                    p.completionTime, p.getTurnaroundTime(), p.getWaitingTime());

            totalWaitingTime += p.getWaitingTime();
            totalTurnaroundTime += p.getTurnaroundTime();
        }

        System.out.println("\n=== 통계 ===");
        System.out.printf("평균 대기 시간: %.2f\n", totalWaitingTime / processes.size());
        System.out.printf("평균 반환 시간: %.2f\n", totalTurnaroundTime / processes.size());
        System.out.println("=".repeat(70));
    }
}
