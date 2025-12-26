package scheduler.core;

import scheduler.model.PCB;
import scheduler.policy.SchedulingPolicy;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private final SchedulingPolicy policy;
    private final SimulationClock clock = new SimulationClock();
    private final ReadyQueue readyQueue;
    private final WaitingQueue waitingQueue = new WaitingQueue();
    private final Dispatcher dispatcher;

    private Thread cpuThread;
    private Thread ioThread;

    private final List<PCB> processes = new ArrayList<>();

    public Scheduler(SchedulingPolicy policy, int contextSwitchCost) {
        this.policy = policy;
        this.readyQueue = new ReadyQueue(policy);
        this.dispatcher = new Dispatcher(clock, contextSwitchCost);
    }

    public void addPCB(PCB pcb) {
        processes.add(pcb);
    }

    public void start() {
        processes.stream()
                .sorted((a, b) -> Integer.compare(a.getArrivalTime(), b.getArrivalTime()))
                .forEach(pcb -> {
                    clock.set(Math.max(clock.now(), pcb.getArrivalTime()));
                    pcb.enterReady(clock.now());
                    readyQueue.add(pcb);
                    dispatcher.onArrival(pcb);
                });

        CpuCore core = new CpuCore(readyQueue, waitingQueue, policy, clock, dispatcher);
        IOWorker ioWorker = new IOWorker(waitingQueue, readyQueue, clock);
        cpuThread = new Thread(core, "CPU-Core");
        ioThread = new Thread(ioWorker, "IO-Worker");
        cpuThread.start();
        ioThread.start();

        // wait until all processes terminate
        while (true) {
            long alive = processes.stream()
                    .filter(p -> p.getState() != PCB.State.TERMINATED)
                    .count();
            if (alive == 0 && readyQueue.isEmpty() && waitingQueue.isEmpty())
                break;
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }

        core.stop();
        ioWorker.stop();

        cpuThread.interrupt();
        ioThread.interrupt();
    }

    public void printReport() {
        System.out.println("Policy: " + policy.name());
        for (PCB p : processes) {
            System.out.printf(
                    "PID=%d arrival=%d resp=%d wait=%d turn=%d state=%s\n",
                    p.getPid(), p.getArrivalTime(),  p.getResponseTime(), p.getWaitingTime(), p.getTurnaroundTime(), p.getState()
            );
        }
        System.out.printf("Clock=%d contextSwitches=%d\n", clock.now(), dispatcher.getContextSwitches());
    }
}
