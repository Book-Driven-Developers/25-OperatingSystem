package operation_system.cpu_scheduling.algorithm;

import java.util.Queue;
import operation_system.cpu_scheduling.entity.PCB;

/**
 * Round Robin (RR) 스케줄링 알고리즘
 *
 * 특징:
 * - 각 프로세스에게 동일한 시간 할당량(Time Quantum)을 부여
 * - 선점형(Preemptive) 스케줄링
 * - 시간 할당량이 끝나면 프로세스를 준비 큐의 맨 뒤로 이동
 * - 모든 프로세스가 공평하게 CPU를 사용
 *
 * Time Quantum 설정:
 * - 너무 크면: FCFS와 유사해짐
 * - 너무 작으면: Context Switching 오버헤드 증가
 */
public class RoundRobinScheduling implements SchedulingAlgorithm{

    private final int timeQuantum; // 시간 할당량 (ms)

    public RoundRobinScheduling(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    @Override
    public PCB selectNextProcess(Queue<PCB> readyQueue) {
        if (readyQueue == null || readyQueue.isEmpty()) {
            return null;
        }
        // 준비 큐의 맨 앞에 있는 프로세스 선택
        // 시간 할당량만큼 실행 후 다시 큐의 맨 뒤로 이동
        return readyQueue.poll();
    }

    @Override
    public String getAlgorithmName() {
        return "Round Robin (Time Quantum: " + timeQuantum + "ms)";
    }

    @Override
    public void addToReadyQueue(Queue<PCB> readyQueue, PCB pcb) {
        // RR은 FIFO 순서로 큐에 추가
        readyQueue.offer(pcb);
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    // 스케줄링 실행
    @Override
    public void run(Queue<PCB> readyQueue) {
        int time = 0;

        if(readyQueue.isEmpty()){
            System.out.println("처리 할 프로세스가 없음");
            return;
        }

        while(!readyQueue.isEmpty()){
            PCB pcb = readyQueue.poll();

            int remainTime = Math.max(pcb.getCpuScheduledInfo().getRemainingTime() - timeQuantum, 0);
            time += Math.min(timeQuantum,pcb.getCpuScheduledInfo().getRemainingTime());
            System.out.println("처리 프로세스 ID : " + pcb.getPid() + " 남은 처리량 " + remainTime +"     [소요시간] : "  + time);
            // 작업이 안끝났으면 뒤로
            if(remainTime > 0){
                pcb.getCpuScheduledInfo().setRemainingTime(remainTime);
                readyQueue.offer(pcb);
            }
        }
    }
}
