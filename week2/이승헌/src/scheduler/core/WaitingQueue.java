package scheduler.core;

import scheduler.model.PCB;

import java.util.concurrent.LinkedBlockingQueue;

public class WaitingQueue {
    private final LinkedBlockingQueue<PCB> queue = new LinkedBlockingQueue<>();

    public void add(PCB pcb) {
        queue.add(pcb);
    }

    public PCB poll() throws InterruptedException {
        return queue.take();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

