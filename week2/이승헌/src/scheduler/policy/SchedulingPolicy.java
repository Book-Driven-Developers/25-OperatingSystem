package scheduler.policy;

import scheduler.model.PCB;

import java.util.Comparator;

public interface SchedulingPolicy {
    String name();

    Comparator<PCB> comparator();

    int quantum(); // in ticks; if <=0 means non-preemptive

    boolean shouldPreempt(PCB current, PCB candidate);
}

