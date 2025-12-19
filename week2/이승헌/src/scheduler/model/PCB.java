package scheduler.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PCB {
    public enum State {NEW, READY, RUNNING, IO_WAIT, TERMINATED}

    private final int pid;
    private final int arrivalTime;
    private final List<Burst> bursts;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private volatile State state = State.NEW;

    // Metrics
    private int firstResponseTime = -1;
    private int waitingTime = 0;
    private int lastQueueEnterTime = 0;
    private int startTime = -1;
    private int endTime = -1;

    // Remaining in current burst (mutable)
    private int remaining;

    public PCB(int pid, int arrivalTime, List<Burst> bursts) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.bursts = List.copyOf(bursts);
        this.remaining = bursts.isEmpty() ? 0 : bursts.get(0).getDuration();
    }

    public static PCB of(int pid, int arrival, Burst... bursts) {
        return new PCB(pid, arrival, List.of(bursts));
    }

    public synchronized void enterReady(int now) {
        state = State.READY;
        lastQueueEnterTime = now;
    }

    public synchronized void enterRunning(int now) {
        state = State.RUNNING;
        if (firstResponseTime == -1)
            firstResponseTime = now - arrivalTime;
        if (startTime == -1)
            startTime = now;
        // accumulate waiting time since lastQueueEnter
        if (lastQueueEnterTime > 0) {
            waitingTime += now - lastQueueEnterTime;
        }
    }

    public synchronized void enterIoWait() {
        state = State.IO_WAIT;
    }

    public synchronized void terminate(int now) {
        state = State.TERMINATED;
        endTime = now;
    }

    public synchronized Burst getCurrentBurst() {
        int idx = currentIndex.get();
        if (idx >= bursts.size()) return null;
        return bursts.get(idx);
    }

    public synchronized boolean advance(int ticks) {
        if (state == State.TERMINATED) return false;
        Burst b = getCurrentBurst();
        if (b == null) return false;
        int use = Math.min(remaining, ticks);
        remaining -= use;
        return remaining == 0;
    }

    public synchronized Burst nextBurst() {
        int idx = currentIndex.incrementAndGet();
        if (idx < bursts.size()) {
            remaining = bursts.get(idx).getDuration();
        } else {
            remaining = 0;
        }

        return getCurrentBurst();
    }

    public boolean isComplete() {
        return currentIndex.get() >= bursts.size();
    }

    public int getPid() {
        return pid;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getLastQueueEnterTime() {
        return lastQueueEnterTime;
    }

    public State getState() {
        return state;
    }

    public int getRemaining() {
        return remaining;
    }

    // Metrics accessors
    public int getWaitingTime() {
        return waitingTime;
    }

    public int getResponseTime() {
        return firstResponseTime;
    }

    public int getTurnaroundTime() {
        return endTime - arrivalTime;
    }

}

