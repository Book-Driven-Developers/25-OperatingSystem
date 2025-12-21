package operation_system.cpu_scheduling.algorithm;

import java.util.List;
import java.util.Queue;
import operation_system.cpu_scheduling.entity.PCB;

/**
 * CPU 스케줄링 알고리즘 인터페이스
 * Strategy 패턴을 사용하여 다양한 스케줄링 알고리즘을 구현할 수 있도록 함
 */
public interface SchedulingAlgorithm {

    /**
     * 준비 큐에서 다음에 실행할 프로세스를 선택
     * @param readyQueue 준비 상태의 프로세스 목록
     * @return 다음에 실행할 PCB, 없으면 null
     */
    PCB selectNextProcess(Queue<PCB> readyQueue);

    /**
     * 알고리즘 이름 반환
     * @return 스케줄링 알고리즘 이름
     */
    String getAlgorithmName();

    /**
     * 프로세스를 준비 큐에 추가할 때 호출
     * 일부 알고리즘은 큐에 추가할 때 정렬이 필요할 수 있음
     * @param readyQueue 준비 큐
     * @param pcb 추가할 PCB
     */
    void addToReadyQueue(Queue<PCB> readyQueue, PCB pcb);

    void run(Queue<PCB> readyQueue);
}

