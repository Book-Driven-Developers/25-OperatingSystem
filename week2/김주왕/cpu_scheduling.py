#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Chapter 11 CPU Scheduling Demo
- FCFS
- SJF (non-preemptive)
- SRTF (preemptive SJF)
- Round Robin
- Priority (non-preemptive + preemptive)
- MLFQ (simple 3-level feedback queue)

Goal:
- produce a clean, copy-pastable output for Markdown study notes.
"""

from __future__ import annotations
from dataclasses import dataclass, field
from typing import List, Dict, Tuple, Optional
from collections import deque


@dataclass
class Process:
    pid: str
    arrival: int
    burst: int
    priority: int = 0  # higher number => higher priority (for this demo)
    remaining: int = field(init=False)

    # Metrics
    start_time: Optional[int] = None
    completion: Optional[int] = None
    first_run: Optional[int] = None

    def __post_init__(self):
        self.remaining = self.burst


@dataclass
class Segment:
    pid: str
    start: int
    end: int


def _reset(processes: List[Process]) -> List[Process]:
    # rebuild objects (lightweight deep copy)
    return [Process(p.pid, p.arrival, p.burst, p.priority) for p in processes]


def _compact(segments: List[Segment]) -> List[Segment]:
    if not segments:
        return segments
    out = [segments[0]]
    for s in segments[1:]:
        last = out[-1]
        if s.pid == last.pid and s.start == last.end:
            out[-1] = Segment(last.pid, last.start, s.end)
        else:
            out.append(s)
    return out


def _gantt(segments: List[Segment], title: str) -> str:
    segments = _compact(segments)
    label_line = []
    for s in segments:
        width = max(3, s.end - s.start)
        label_line.append(s.pid.center(width))

    times = [segments[0].start] + [s.end for s in segments]
    widths = [max(3, s.end - s.start) for s in segments]
    pos = [0]
    for w in widths:
        pos.append(pos[-1] + w)
    total = pos[-1]

    # separator line
    sep = [" "] * (total + 1)
    for p in pos:
        if p <= total:
            sep[p] = "|"
    sep_line = "".join(sep)

    # time line
    tbuf = [" "] * (total + 1)
    for p, t in zip(pos, times):
        ts = str(t)
        for i, ch in enumerate(ts):
            if p + i <= total:
                tbuf[p + i] = ch
    time_line = "".join(tbuf)

    return (
        f"{title}\n"
        f"{' '.join(label_line)}\n"
        f"{sep_line}\n"
        f"{time_line}"
    )


def _metrics(processes: List[Process]) -> Dict[str, Dict[str, float]]:
    rows: Dict[str, Dict[str, float]] = {}
    for p in processes:
        assert p.completion is not None
        tat = p.completion - p.arrival
        wt = tat - p.burst
        rt = (p.first_run - p.arrival) if p.first_run is not None else 0
        rows[p.pid] = {
            "arrival": p.arrival, "burst": p.burst, "priority": p.priority,
            "completion": p.completion, "turnaround": tat,
            "waiting": wt, "response": rt,
        }
    return rows


def _metrics_table(rows: Dict[str, Dict[str, float]]) -> str:
    pids = sorted(rows.keys(), key=lambda x: (len(x), x))
    headers = ["PID", "AT", "BT", "PR", "CT", "TAT", "WT", "RT"]
    lines = ["  ".join(h.rjust(3) for h in headers)]
    lines.append("  ".join("---".rjust(3) for _ in headers))
    for pid in pids:
        r = rows[pid]
        lines.append("  ".join([
            pid.rjust(3),
            str(int(r["arrival"])).rjust(3),
            str(int(r["burst"])).rjust(3),
            str(int(r["priority"])).rjust(3),
            str(int(r["completion"])).rjust(3),
            str(int(r["turnaround"])).rjust(3),
            str(int(r["waiting"])).rjust(3),
            str(int(r["response"])).rjust(3),
        ]))
    avg = {k: sum(rows[p][k] for p in rows)/len(rows) for k in ["turnaround", "waiting", "response"]}
    lines.append("")
    lines.append(f"AVG TAT={avg['turnaround']:.2f}, AVG WT={avg['waiting']:.2f}, AVG RT={avg['response']:.2f}")
    return "\n".join(lines)


def fcfs(processes: List[Process]):
    procs = _reset(processes)
    t = 0
    segs: List[Segment] = []
    for p in sorted(procs, key=lambda x: (x.arrival, x.pid)):
        if t < p.arrival:
            segs.append(Segment("IDLE", t, p.arrival))
            t = p.arrival
        if p.first_run is None:
            p.first_run = t
        segs.append(Segment(p.pid, t, t + p.burst))
        t += p.burst
        p.remaining = 0
        p.completion = t
    return segs, procs


def sjf_nonpreemptive(processes: List[Process]):
    procs = _reset(processes)
    t = 0
    done = 0
    n = len(procs)
    segs: List[Segment] = []
    while done < n:
        ready = [p for p in procs if p.arrival <= t and p.completion is None]
        if not ready:
            nxt = min(p.arrival for p in procs if p.completion is None)
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            continue
        p = min(ready, key=lambda x: (x.burst, x.arrival, x.pid))
        if p.first_run is None:
            p.first_run = t
        segs.append(Segment(p.pid, t, t + p.burst))
        t += p.burst
        p.remaining = 0
        p.completion = t
        done += 1
    return segs, procs


def srtf_preemptive(processes: List[Process]):
    procs = _reset(processes)
    t = 0
    done = 0
    n = len(procs)
    segs: List[Segment] = []
    while done < n:
        ready = [p for p in procs if p.arrival <= t and p.completion is None]
        if not ready:
            nxt = min(p.arrival for p in procs if p.completion is None)
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            continue
        p = min(ready, key=lambda x: (x.remaining, x.arrival, x.pid))
        if p.first_run is None:
            p.first_run = t
        segs.append(Segment(p.pid, t, t + 1))
        p.remaining -= 1
        t += 1
        if p.remaining == 0:
            p.completion = t
            done += 1
    return segs, procs


def round_robin(processes: List[Process], quantum: int = 2):
    procs = _reset(processes)
    t = 0
    segs: List[Segment] = []
    q = deque()
    procs_sorted = sorted(procs, key=lambda x: (x.arrival, x.pid))
    i = 0
    done = 0
    n = len(procs)

    def push_arrivals(up_to: int):
        nonlocal i
        while i < n and procs_sorted[i].arrival <= up_to:
            q.append(procs_sorted[i])
            i += 1

    push_arrivals(t)
    while done < n:
        if not q:
            nxt = procs_sorted[i].arrival
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            push_arrivals(t)
            continue
        p = q.popleft()
        if p.first_run is None:
            p.first_run = t
        run = min(quantum, p.remaining)
        segs.append(Segment(p.pid, t, t + run))
        for _ in range(run):
            t += 1
            p.remaining -= 1
            push_arrivals(t)
            if p.remaining == 0:
                p.completion = t
                done += 1
                break
        if p.completion is None:
            q.append(p)
    return segs, procs


def priority_nonpreemptive(processes: List[Process]):
    procs = _reset(processes)
    t = 0
    done = 0
    n = len(procs)
    segs: List[Segment] = []
    while done < n:
        ready = [p for p in procs if p.arrival <= t and p.completion is None]
        if not ready:
            nxt = min(p.arrival for p in procs if p.completion is None)
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            continue
        p = max(ready, key=lambda x: (x.priority, -x.arrival, x.pid))
        if p.first_run is None:
            p.first_run = t
        segs.append(Segment(p.pid, t, t + p.remaining))
        t += p.remaining
        p.remaining = 0
        p.completion = t
        done += 1
    return segs, procs


def priority_preemptive(processes: List[Process]):
    procs = _reset(processes)
    t = 0
    done = 0
    n = len(procs)
    segs: List[Segment] = []
    while done < n:
        ready = [p for p in procs if p.arrival <= t and p.completion is None]
        if not ready:
            nxt = min(p.arrival for p in procs if p.completion is None)
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            continue
        p = max(ready, key=lambda x: (x.priority, -x.arrival, x.pid))
        if p.first_run is None:
            p.first_run = t
        segs.append(Segment(p.pid, t, t + 1))
        p.remaining -= 1
        t += 1
        if p.remaining == 0:
            p.completion = t
            done += 1
    return segs, procs


def mlfq(processes: List[Process], q0: int = 1, q1: int = 2, q2: int = 4):
    """
    Simple MLFQ:
    - 3 queues: Q0 (highest), Q1, Q2 (lowest)
    - New arrivals enter Q0
    - If a process uses its whole quantum without finishing, it is demoted
    - Preemption: checked every 1 time unit
    """
    procs = _reset(processes)
    segs: List[Segment] = []
    t = 0
    done = 0
    n = len(procs)
    procs_sorted = sorted(procs, key=lambda x: (x.arrival, x.pid))
    i = 0

    Q0, Q1, Q2 = deque(), deque(), deque()

    def add_arrivals(now: int):
        nonlocal i
        while i < n and procs_sorted[i].arrival <= now:
            Q0.append(procs_sorted[i])
            i += 1

    add_arrivals(t)

    def pick():
        if Q0:
            return Q0, q0
        if Q1:
            return Q1, q1
        if Q2:
            return Q2, q2
        return None, 0

    while done < n:
        qref, quantum = pick()
        if qref is None:
            nxt = procs_sorted[i].arrival
            if t < nxt:
                segs.append(Segment("IDLE", t, nxt))
                t = nxt
            add_arrivals(t)
            continue

        p = qref.popleft()
        if p.first_run is None:
            p.first_run = t

        used = 0
        while used < quantum and p.remaining > 0:
            segs.append(Segment(p.pid, t, t + 1))
            p.remaining -= 1
            t += 1
            used += 1
            add_arrivals(t)

            if Q0 and qref is not Q0:
                break
            if Q1 and qref is Q2 and not Q0:
                break

        if p.remaining == 0:
            p.completion = t
            done += 1
            continue

        if used < quantum:
            qref.append(p)
        else:
            if qref is Q0:
                Q1.append(p)
            elif qref is Q1:
                Q2.append(p)
            else:
                Q2.append(p)

    return segs, procs


def demo():
    processes = [
        Process("P1", arrival=0, burst=8, priority=2),
        Process("P2", arrival=1, burst=4, priority=5),
        Process("P3", arrival=2, burst=9, priority=1),
        Process("P4", arrival=3, burst=5, priority=4),
    ]

    print("=== Chapter 11 CPU Scheduling Demo ===")
    print("Workload (AT=Arrival Time, BT=Burst Time, PR=Priority; higher PR => higher priority)")
    for p in processes:
        print(f"- {p.pid}: AT={p.arrival}, BT={p.burst}, PR={p.priority}")
    print()

    algos = [
        ("FCFS (Non-preemptive)", lambda: fcfs(processes)),
        ("SJF (Non-preemptive)", lambda: sjf_nonpreemptive(processes)),
        ("SRTF (Preemptive SJF)", lambda: srtf_preemptive(processes)),
        ("Round Robin (q=2)", lambda: round_robin(processes, quantum=2)),
        ("Priority (Non-preemptive)", lambda: priority_nonpreemptive(processes)),
        ("Priority (Preemptive)", lambda: priority_preemptive(processes)),
        ("MLFQ (q0=1,q1=2,q2=4)", lambda: mlfq(processes, q0=1, q1=2, q2=4)),
    ]

    for title, fn in algos:
        segs, procs = fn()
        print(_gantt(segs, f"[{title}] Gantt Chart"))
        print()
        rows = _metrics(procs)
        print(_metrics_table(rows))
        print("\n" + "-" * 60 + "\n")


if __name__ == "__main__":
    demo()
