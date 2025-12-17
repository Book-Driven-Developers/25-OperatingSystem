package com.application.scheduler;

import java.util.List;

public interface Scheduler {
    void schedule(List<Process> processes);
}
