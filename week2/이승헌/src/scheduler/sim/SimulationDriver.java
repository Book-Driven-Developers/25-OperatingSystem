package scheduler.sim;

import scheduler.core.Scheduler;
import scheduler.model.Burst;
import scheduler.model.PCB;
import scheduler.policy.FcfsPolicy;
import scheduler.policy.RoundRobinPolicy;

public class SimulationDriver {
    public static void main(String[] args) {
//        RoundRobinPolicy policy = new RoundRobinPolicy(4);
        FcfsPolicy policy = new FcfsPolicy();
        Scheduler scheduler = new Scheduler(policy, 1);

        PCB pcb1 = PCB.of(
                1,
                0,
                new Burst(Burst.Type.CPU, 3),
                new Burst(Burst.Type.IO, 5),
                new Burst(Burst.Type.CPU, 4)
        );

        PCB pcb2 = PCB.of(
                2,
                2,
                new Burst(Burst.Type.CPU, 6)
        );

        PCB pcb3 = PCB.of(
                3,
                4,
                new Burst(Burst.Type.CPU, 2),
                new Burst(Burst.Type.IO, 3),
                new Burst(Burst.Type.CPU, 1)
        );

        scheduler.addPCB(pcb1);
        scheduler.addPCB(pcb2);
        scheduler.addPCB(pcb3);

        scheduler.start();
        scheduler.printReport();
    }
}
