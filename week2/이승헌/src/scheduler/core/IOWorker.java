package scheduler.core;

import scheduler.model.Burst;
import scheduler.model.PCB;

import java.util.concurrent.atomic.AtomicBoolean;

public class IOWorker implements Runnable {
    private final WaitingQueue waitingQueue;
    private final ReadyQueue readyQueue;
    private final SimulationClock clock;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public IOWorker(WaitingQueue waitingQueue, ReadyQueue readyQueue, SimulationClock clock) {
        this.waitingQueue = waitingQueue;
        this.readyQueue = readyQueue;
        this.clock = clock;
    }

    public void start() { running.set(true); }
    public void stop() { running.set(false); }

    @Override
    public void run() {
        start();
        while (running.get()) {
            try {
                PCB pcb = waitingQueue.poll();
                Burst burst = pcb.getCurrentBurst();
                if (burst == null || burst.getType() != Burst.Type.IO) {
                    pcb.nextBurst();
                    continue;
                }
                int duration = burst.getDuration();
                clock.advance(duration);
                pcb.nextBurst();
                // back to ready
                pcb.enterReady(clock.now());
                readyQueue.add(pcb);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

