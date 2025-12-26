package scheduler.core;

import java.util.concurrent.atomic.AtomicInteger;

public class SimulationClock {
    private final AtomicInteger now = new AtomicInteger(0);

    public void set(int t) {
        now.set(t);
    }

    public int advance(int delta) {
        return now.addAndGet(delta);
    }

    public int now() {
        return now.get();
    }
}
