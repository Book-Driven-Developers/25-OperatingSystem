package operation_system.cpu_scheduling.algorithm;

import java.util.List;
import java.util.Queue;
import operation_system.cpu_scheduling.entity.PCB;

public class FCFSScheduling implements SchedulingAlgorithm{
    @Override
    public PCB selectNextProcess(Queue<PCB> readyQueue) {
        if (readyQueue == null || readyQueue.isEmpty()) {
            return null;
        }
        // 준비 큐의 맨 앞에 있는 프로세스 선택 (먼저 도착한 프로세스)
        return readyQueue.poll();
    }

    @Override
    public String getAlgorithmName() {
        return "FCFS (First-Come, First-Served)";
    }

    @Override
    public void addToReadyQueue(Queue<PCB> readyQueue, PCB pcb) {
        // FCFS는 도착 순서대로 큐에 추가
        readyQueue.offer(pcb);
    }

    @Override
    public void run(Queue<PCB> readyQueue) {
        int time = 0;

        if(readyQueue.isEmpty()){
            System.out.println("처리 할 프로세스가 없음");
            return;
        }

        while(!readyQueue.isEmpty()){
            PCB pcb = readyQueue.poll();

            // 순차적 처리
            time += pcb.getCpuScheduledInfo().getRemainingTime();
            System.out.println("처리 프로세스 ID : " + pcb.getPid() + " 남은 처리량 " + pcb.getCpuScheduledInfo().getRemainingTime() +"     [소요시간] : "  + time);
        }
    }
}
