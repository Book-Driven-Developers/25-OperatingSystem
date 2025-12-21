package operation_system.cpu_scheduling.algorithm;

import java.util.ArrayDeque;
import java.util.Queue;
import operation_system.cpu_scheduling.entity.PCB;

public class Scheduler {
    
    private SchedulingAlgorithm algorithm; // 현재 사용 중인 스케줄링 알고리즘
    private final Queue<PCB> readyQueue;    // 준비 큐 (READY 상태의 프로세스들)


    public Scheduler(SchedulingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.readyQueue = new ArrayDeque<>();
    }

    /**
     * 다음에 실행할 프로세스를 선택
     * @return 선택된 PCB, 준비 큐가 비어있으면 null
     */
    public PCB selectNextProcess() {
        return algorithm.selectNextProcess(readyQueue);
    }

    /**
     * 프로세스를 준비 큐에 추가
     * @param pcb 추가할 PCB
     */
    //TODO : 동시성 제어를 위해 synchronized 고려할 것
    public void addToReadyQueue(PCB pcb) {
        if (pcb == null) {
            throw new IllegalArgumentException("PCB cannot be null");
        }
        algorithm.addToReadyQueue(readyQueue, pcb);
    }

    /**
     * 준비 큐에서 프로세스 제거
     * @param pcb 제거할 PCB
     * @return 제거 성공 여부
     */
    public boolean removeFromReadyQueue(PCB pcb) {
        return readyQueue.remove(pcb);
    }

    /**
     * 준비 큐가 비어있는지 확인
     * @return 비어있으면 true
     */
    public boolean isReadyQueueEmpty() {
        return readyQueue.isEmpty();
    }

    /**
     * 준비 큐 크기 반환
     * @return 준비 큐의 프로세스 개수
     */
    public int getReadyQueueSize() {
        return readyQueue.size();
    }

    /**
     * 현재 사용 중인 알고리즘 이름 반환
     * @return 알고리즘 이름
     */
    public String getCurrentAlgorithmName() {
        return algorithm.getAlgorithmName();
    }

    public void run(){
        algorithm.run(readyQueue);
    }
    
}
