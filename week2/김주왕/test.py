from collections import deque
from dataclasses import dataclass, field
from typing import List


@dataclass
class Process:
    pid: str
    arrival_time: int
    burst_time: int
    priority: int = 0
    remaining_time: int = field(init=False)

    def __post_init__(self):
        self.remaining_time = self.burst_time


def fcfs(processes: List[Process]):
    print("\n[FCFS Scheduling]")
    time = 0
    for p in sorted(processes, key=lambda x: x.arrival_time):
        if time < p.arrival_time:
            time = p.arrival_time
        print(f"time {time} -> {time + p.burst_time}: {p.pid}")
        time += p.burst_time


def sjf(processes: List[Process]):
    print("\n[SJF Scheduling]")
    time = 0
    completed = []
    ready = []

    processes = sorted(processes, key=lambda x: x.arrival_time)
    while len(completed) < len(processes):
        for p in processes:
            if p.arrival_time <= time and p not in ready and p not in completed:
                ready.append(p)

        if ready:
            p = min(ready, key=lambda x: x.burst_time)
            ready.remove(p)
            print(f"time {time} -> {time + p.burst_time}: {p.pid}")
            time += p.burst_time
            completed.append(p)
        else:
            time += 1


def priority_scheduling(processes: List[Process]):
    print("\n[Priority Scheduling]")
    time = 0
    completed = []
    ready = []

    while len(completed) < len(processes):
        for p in processes:
            if p.arrival_time <= time and p not in ready and p not in completed:
                ready.append(p)

        if ready:
            p = min(ready, key=lambda x: x.priority)
            ready.remove(p)
            print(f"time {time} -> {time + p.burst_time}: {p.pid}")
            time += p.burst_time
            completed.append(p)
        else:
            time += 1


def round_robin(processes: List[Process], quantum=2):
    print("\n[Round Robin Scheduling]")
    time = 0
    queue = deque(sorted(processes, key=lambda x: x.arrival_time))

    while queue:
        p = queue.popleft()
        exec_time = min(quantum, p.remaining_time)
        print(f"time {time} -> {time + exec_time}: {p.pid}")
        time += exec_time
        p.remaining_time -= exec_time

        if p.remaining_time > 0:
            queue.append(p)


if __name__ == "__main__":
    processes = [
        Process("P1", arrival_time=0, burst_time=5, priority=2),
        Process("P2", arrival_time=1, burst_time=3, priority=1),
        Process("P3", arrival_time=2, burst_time=8, priority=3),
    ]

    fcfs(processes)
    sjf(processes)
    priority_scheduling(processes)
    round_robin(processes)
