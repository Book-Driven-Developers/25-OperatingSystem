# -*- coding: utf-8 -*-
"""
bankers_avoidance.py
--------------------
Deadlock avoidance demo using Banker's Algorithm safety check.

Key concepts:
- Available: remaining resource vector
- Max: maximum demand matrix
- Allocation: current allocation matrix
- Need = Max - Allocation
- Safe state exists if we can find a safe sequence.
"""

from __future__ import annotations
from typing import List, Tuple
from common import banner


def bankers_safe_sequence(
    available: List[int],
    max_demand: List[List[int]],
    allocation: List[List[int]],
) -> Tuple[bool, List[int]]:
    n_proc = len(max_demand)
    n_res = len(available)

    # Need = Max - Allocation
    need: List[List[int]] = []
    for p in range(n_proc):
        row = []
        for r in range(n_res):
            row.append(max_demand[p][r] - allocation[p][r])
        need.append(row)

    work = available[:]           # copy
    finish = [False] * n_proc
    seq: List[int] = []

    made_progress = True
    while made_progress:
        made_progress = False

        for p in range(n_proc):
            if finish[p]:
                continue

            # Check if Need[p] <= Work
            can_finish = True
            for r in range(n_res):
                if need[p][r] > work[r]:
                    can_finish = False
                    break

            if can_finish:
                # Simulate p finishing and releasing its allocated resources
                for r in range(n_res):
                    work[r] += allocation[p][r]
                finish[p] = True
                seq.append(p)
                made_progress = True

    safe = all(finish)
    return safe, seq


def run_demo() -> None:
    banner("Banker's Algorithm Demo: Safe state / Safe sequence")

    # Case 1: safe
    available1 = [3, 3, 2]
    max1 = [
        [7, 5, 3],
        [3, 2, 2],
        [9, 0, 2],
        [2, 2, 2],
        [4, 3, 3],
    ]
    alloc1 = [
        [0, 1, 0],
        [2, 0, 0],
        [3, 0, 2],
        [2, 1, 1],
        [0, 0, 2],
    ]
    safe1, seq1 = bankers_safe_sequence(available1, max1, alloc1)
    print(f"[Case1] Safe? {safe1} | Safe sequence: {seq1}")

    # Case 2: unsafe (may have no safe sequence)
    available2 = [0, 1, 0]
    max2 = [
        [2, 1, 1],
        [1, 1, 1],
        [1, 3, 1],
    ]
    alloc2 = [
        [1, 0, 1],
        [0, 1, 0],
        [1, 1, 0],
    ]
    safe2, seq2 = bankers_safe_sequence(available2, max2, alloc2)
    print(f"[Case2] Safe? {safe2} | Safe sequence: {seq2}\n")


if __name__ == "__main__":
    run_demo()
