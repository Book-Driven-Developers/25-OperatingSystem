package operation_system.cpu_scheduling;

import operation_system.cpu_scheduling.algorithm.FCFSScheduling;
import operation_system.cpu_scheduling.algorithm.RoundRobinScheduling;
import operation_system.cpu_scheduling.algorithm.Scheduler;
import operation_system.cpu_scheduling.algorithm.SchedulingAlgorithm;
import operation_system.cpu_scheduling.entity.PCB;
import operation_system.cpu_scheduling.entity.SchedulingInfo;

import java.util.ArrayList;
import java.util.List;

public class CpuSchedulingMain {
    public static void main(String[] args) {

        // PCB 리스트 할당
        List<PCB> pcbList = createProcess();


        System.out.println("===================== Round Robin =====================");
        SchedulingAlgorithm rr = new RoundRobinScheduling(5);
        Scheduler scheduler = new Scheduler(rr);
        for(PCB pcb : pcbList){
            scheduler.addToReadyQueue(pcb);
        }
        scheduler.run();

        System.out.println();
        System.out.println("===================== FCFS =====================");
        SchedulingAlgorithm fcfs = new FCFSScheduling();
        scheduler = new Scheduler(fcfs);
        pcbList = createProcess();
        for(PCB pcb : pcbList){
            scheduler.addToReadyQueue(pcb);
        }
        scheduler.run();

    }

    public static List<PCB> createProcess(){
        List<PCB> pcbList = new ArrayList<>();
        // 제공된 스케줄링 정보
        SchedulingInfo schedulingInfoA = new SchedulingInfo(0, 10);
        SchedulingInfo schedulingInfoB = new SchedulingInfo(0, 5);
        SchedulingInfo schedulingInfoC = new SchedulingInfo(0, 20);
        SchedulingInfo schedulingInfoD = new SchedulingInfo(0, 8);

        // PCB (프로세스 정보)
        pcbList.add(new PCB(1,"Ready",schedulingInfoA));
        pcbList.add(new PCB(2,"Ready",schedulingInfoB));
        pcbList.add(new PCB(3,"Ready",schedulingInfoC));
        pcbList.add(new PCB(4,"Ready",schedulingInfoD));

        return pcbList;
    }
}
