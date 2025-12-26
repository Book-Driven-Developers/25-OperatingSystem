package scheduler.policy;

import scheduler.model.PCB;

import java.util.Comparator;

public class FcfsPolicy implements SchedulingPolicy {
    private static final Comparator<PCB> COMP =
            Comparator.comparingInt(PCB::getArrivalTime)
                    .thenComparingInt(PCB::getPid);

    @Override
    public String name() {
        return "FCFS";
    }

    @Override
    public Comparator<PCB> comparator() {
        return COMP;
    }

    @Override
    public int quantum() {
        return 0;
    }

    @Override
    public boolean shouldPreempt(PCB current, PCB candidate) {
        return false; // non-preemptive
    }
}

