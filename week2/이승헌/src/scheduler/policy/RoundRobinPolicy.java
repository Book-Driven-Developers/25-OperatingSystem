package scheduler.policy;

import scheduler.model.PCB;

import java.util.Comparator;

public class RoundRobinPolicy implements SchedulingPolicy {
    private final int quantum;

    public RoundRobinPolicy(int quantum) {
        if (quantum <= 0) throw new IllegalArgumentException("quantum must be > 0");
        this.quantum = quantum;
    }

    @Override
    public String name() {
        return "RoundRobin";
    }

    @Override
    public Comparator<PCB> comparator() {
        return Comparator.comparingInt(PCB::getLastQueueEnterTime)
                .thenComparingInt(PCB::getPid);
    }

    @Override
    public int quantum() {
        return quantum;
    }

    @Override
    public boolean shouldPreempt(PCB current, PCB candidate) {
        return false;
    }
}

