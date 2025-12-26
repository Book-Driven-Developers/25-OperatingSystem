package scheduler.core;

import scheduler.model.Burst;
import scheduler.model.PCB;
import scheduler.policy.SchedulingPolicy;

import java.util.concurrent.atomic.AtomicBoolean;

public class CpuCore implements Runnable {
    private final ReadyQueue readyQueue;
    private final WaitingQueue waitingQueue;
    private final SchedulingPolicy policy;
    private final SimulationClock clock;
    private final Dispatcher dispatcher;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CpuCore(ReadyQueue readyQueue, WaitingQueue waitingQueue, SchedulingPolicy policy, SimulationClock clock, Dispatcher dispatcher) {
        this.readyQueue = readyQueue;
        this.waitingQueue = waitingQueue;
        this.policy = policy;
        this.clock = clock;
        this.dispatcher = dispatcher;
    }

    public void start() {
        running.set(true);
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        start();
        while (running.get()) {
            PCB pcb = readyQueue.poll();
            if (pcb == null) {
                // idle tick
                clock.advance(1);
                continue;
            }
            pcb.enterRunning(clock.now());

            Burst burst = pcb.getCurrentBurst();
            if (burst == null || burst.getType() != Burst.Type.CPU) {
                pcb.nextBurst();
                continue;
            }

            int quantum = policy.quantum();
            int slice = quantum > 0 ? Math.min(quantum, pcb.getRemaining()) : pcb.getRemaining();

            boolean finished = pcb.advance(slice);
            clock.advance(slice);
            if (finished) {
                Burst next = pcb.nextBurst();
                if (next == null) {
                    pcb.terminate(clock.now());
                    dispatcher.onProcessTerminated(pcb);
                } else if (next.getType() == Burst.Type.IO) {
                    pcb.enterIoWait();
                    waitingQueue.add(pcb);
                } else {
                    pcb.enterReady(clock.now());
                    readyQueue.add(pcb);
                }
            } else { // time slice가 경과했으므로, 다른 프로세스에게 양보하기 위해 준비큐로 이동
                pcb.enterReady(clock.now());
                readyQueue.add(pcb);
            }

            dispatcher.onCpuIdleOrYield();
        }
    }
}

