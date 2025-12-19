package scheduler.core;

import scheduler.model.PCB;

public class Dispatcher {
    private final SimulationClock clock;
    private final int contextSwitchCost;
    private int contextSwitches = 0;

    public Dispatcher(SimulationClock clock, int contextSwitchCost) {
        this.clock = clock;
        this.contextSwitchCost = Math.max(0, contextSwitchCost);
    }

    public void applyContextSwitch() {
        if (contextSwitchCost > 0) {
            clock.advance(contextSwitchCost);
        }
        contextSwitches++;
    }

    public void onArrival(PCB pcb) {
        // hook for future preemption decisions
    }

    public void onCpuIdleOrYield() {
        applyContextSwitch();
    }

    public void onProcessTerminated(PCB pcb) {
        // hook: could log or collect metrics
    }

    public int getContextSwitches() { return contextSwitches; }
}

