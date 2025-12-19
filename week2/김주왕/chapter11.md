# Chapter 11. CPU 스케줄링 — 스터디 정리 노트

> 교재: **혼자 공부하는 컴퓨터구조 + 운영체제** (Chapter 11)

---

## 11-1. CPU 스케줄링 개요

### Summary

- **CPU 스케줄링**: 여러 프로세스가 동시에 준비(Ready) 상태일 때, **누구에게 CPU를 언제/얼마나** 줄지 결정하는 방법(정책).
- **프로세스 우선순위**: 운영체제는 프로세스마다 우선순위를 두어, CPU 자원을 **공정하고 합리적**으로 배분한다.  
  - 우선순위 정보는 보통 **PCB(Process Control Block)**에 기록된다.
- **스케줄링 큐**: 스케줄링을 효율적으로 수행하기 위해 운영체제가 유지하는 큐.
  - **준비 큐(Ready Queue)**: CPU 할당을 기다리는 프로세스들의 큐
  - **대기 큐(Waiting Queue)**: I/O(입출력 장치) 완료를 기다리는 프로세스들의 큐
- **선점형(Preemptive) vs 비선점형(Non-preemptive)**
  - **선점형**: OS가 필요하면 실행 중인 프로세스에서 CPU를 **빼앗아** 다른 프로세스에 할당할 수 있음  
    (응답성↑, 문맥 교환 비용↑ 가능)
  - **비선점형**: 한 번 CPU를 얻으면 **자발적으로 반납(종료/대기)**할 때까지 계속 실행  
    (문맥 교환↓, 긴 작업이 있으면 대기↑ 가능)

---

## 11-2. CPU 스케줄링 알고리즘

### 대표 알고리즘 요약

- **FCFS(선입 선처리)**: 준비 큐에 **들어온 순서대로** CPU 할당
- **SJF(최단 작업 우선)**: 준비 큐에서 **CPU 사용 시간이 가장 짧은** 프로세스부터 CPU 할당 (비선점형)
- **SRTF(Shortest Remaining Time First)**: SJF의 선점형 버전. **남은 시간이 가장 짧은** 프로세스를 우선 실행 (선점형)
- **라운드 로빈(RR)**: 정해진 **시간 할당량(타임 퀀텀)**만큼만 번갈아 CPU 할당 (선점형)
- **우선순위 스케줄링**: 가장 **우선순위가 높은** 프로세스에 CPU 할당 (선점/비선점 모두 가능)
- **다단계 피드백 큐(MLFQ)**: 여러 단계 큐를 두고, 프로세스가 **큐 사이를 이동**하면서 CPU 할당 (선점형으로 운용되는 경우가 많음)

---

## 실습: 스케줄링 알고리즘 데모 코드

아래 코드는 **한 번 실행으로** 여러 알고리즘(FCFS, SJF, SRTF, RR, Priority, MLFQ)을 비교합니다. (Source Code는 LLM사용하여 생성)

- 출력:
  - **Gantt Chart** (CPU 실행 순서)
  - 각 프로세스의 **CT(완료시간), TAT(반환시간), WT(대기시간), RT(응답시간)**
  - 평균 TAT/WT/RT

### 1) 소스 코드

```python
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

```

### 2) 실행 방법

```bash
python chapter11_scheduling_demo.py
```

### 3) 실행 출력

```text
=== Chapter 11 CPU Scheduling Demo ===
Workload (AT=Arrival Time, BT=Burst Time, PR=Priority; higher PR => higher priority)
- P1: AT=0, BT=8, PR=2
- P2: AT=1, BT=4, PR=5
- P3: AT=2, BT=9, PR=1
- P4: AT=3, BT=5, PR=4

[FCFS (Non-preemptive)] Gantt Chart
   P1     P2      P3      P4 
|       |   |        |    |
0       8   12       21   2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2    8    8    0    0
 P2    1    4    5   12   11    7    7
 P3    2    9    1   21   19   10   10
 P4    3    5    4   26   23   18   18

AVG TAT=15.25, AVG WT=8.75, AVG RT=8.75

------------------------------------------------------------

[SJF (Non-preemptive)] Gantt Chart
   P1     P2    P4      P3   
|       |   |    |        |
0       8   12   17       2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2    8    8    0    0
 P2    1    4    5   12   11    7    7
 P3    2    9    1   26   24   15   15
 P4    3    5    4   17   14    9    9

AVG TAT=14.25, AVG WT=7.75, AVG RT=7.75

------------------------------------------------------------

[SRTF (Preemptive SJF)] Gantt Chart
 P1  P2    P4     P1       P3   
|  |   |    |      |        |
0  1   5    10     17       2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2   17   17    9    0
 P2    1    4    5    5    4    0    0
 P3    2    9    1   26   24   15   15
 P4    3    5    4   10    7    2    2

AVG TAT=13.00, AVG WT=6.50, AVG RT=4.25

------------------------------------------------------------

[Round Robin (q=2)] Gantt Chart
 P1  P2  P3  P1  P4  P2  P3  P1  P4  P3  P1  P4  P3
|  |  |  |  |  |  |  |  |  |  |  |  |  |
0  2  4  6  8  10 12 14 16 18 20 22 23 2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2   22   22   14    0
 P2    1    4    5   12   11    7    1
 P3    2    9    1   26   24   15    2
 P4    3    5    4   23   20   15    5

AVG TAT=19.25, AVG WT=12.75, AVG RT=2.00

------------------------------------------------------------

[Priority (Non-preemptive)] Gantt Chart
   P1     P2    P4      P3   
|       |   |    |        |
0       8   12   17       2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2    8    8    0    0
 P2    1    4    5   12   11    7    7
 P3    2    9    1   26   24   15   15
 P4    3    5    4   17   14    9    9

AVG TAT=14.25, AVG WT=7.75, AVG RT=7.75

------------------------------------------------------------

[Priority (Preemptive)] Gantt Chart
 P1  P2    P4     P1       P3   
|  |   |    |      |        |
0  1   5    10     17       2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2   17   17    9    0
 P2    1    4    5    5    4    0    0
 P3    2    9    1   26   24   15   15
 P4    3    5    4   10    7    2    2

AVG TAT=13.00, AVG WT=6.50, AVG RT=4.25

------------------------------------------------------------

[MLFQ (q0=1,q1=2,q2=4)] Gantt Chart
 P1  P2  P3  P4  P1  P2  P3  P4  P1   P2  P3   P4  P1  P3
|  |  |  |  |  |  |  |  |   |  |   |  |  |  |
0  1  2  3  4  6  8  10 12  16 17  21 23 24 2

PID   AT   BT   PR   CT  TAT   WT   RT
---  ---  ---  ---  ---  ---  ---  ---
 P1    0    8    2   24   24   16    0
 P2    1    4    5   17   16   12    0
 P3    2    9    1   26   24   15    0
 P4    3    5    4   23   20   15    0

AVG TAT=21.00, AVG WT=14.50, AVG RT=0.00

------------------------------------------------------------
```
---

## 부록: 용어 빠른 정의

- **CT(Completion Time)**: 프로세스 완료 시각  
- **TAT(Turnaround Time)**: 완료 시각 - 도착 시각  
- **WT(Waiting Time)**: 준비 큐에서 기다린 총 시간 (= TAT - BT)  
- **RT(Response Time)**: 도착 후 **처음 CPU를 받기까지** 걸린 시간  
