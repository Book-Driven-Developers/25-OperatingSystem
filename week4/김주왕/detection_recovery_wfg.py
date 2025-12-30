# -*- coding: utf-8 -*-
"""
detection_recovery_wfg.py
-------------------------
Deadlock detection + recovery using Wait-For Graph (WFG).

WFG definition:
- Node: process
- Edge P -> Q: P waits for Q (Q holds a resource P needs)
If WFG has a cycle => deadlock (or deadlock possible) exists.

Recovery demo:
- "Kill" (remove) one process in the cycle to break the deadlock.
"""

from __future__ import annotations
from typing import Dict, List, Optional, Set, Tuple
from common import banner


def detect_cycle_wait_for_graph(wfg: Dict[str, List[str]]) -> Tuple[bool, List[str]]:
    visited: Set[str] = set()
    stack: Set[str] = set()
    parent: Dict[str, Optional[str]] = {}

    def dfs(u: str) -> Optional[List[str]]:
        visited.add(u)
        stack.add(u)

        for v in wfg.get(u, []):
            if v not in visited:
                parent[v] = u
                cycle = dfs(v)
                if cycle:
                    return cycle
            elif v in stack:
                # Found a back-edge: cycle exists. Reconstruct a path.
                path = [v]
                cur = u
                while cur != v and cur is not None:
                    path.append(cur)
                    cur = parent.get(cur)
                path.append(v)
                path.reverse()
                return path

        stack.remove(u)
        return None

    for node in wfg.keys():
        if node not in visited:
            parent[node] = None
            cycle = dfs(node)
            if cycle:
                return True, cycle

    return False, []


def recover_by_killing_process(wfg: Dict[str, List[str]], victim: str) -> Dict[str, List[str]]:
    # Remove victim node and any edges pointing to it.
    new_wfg: Dict[str, List[str]] = {}
    for p, waits in wfg.items():
        if p == victim:
            continue
        new_wfg[p] = [q for q in waits if q != victim]
    return new_wfg


def run_demo() -> None:
    banner("Deadlock Detection + Recovery Demo (Wait-For Graph)")

    # Example deadlock cycle: P1 -> P2 -> P3 -> P1
    wfg = {
        "P1": ["P2"],
        "P2": ["P3"],
        "P3": ["P1"],
        "P4": [],
    }

    has_cycle, cycle_path = detect_cycle_wait_for_graph(wfg)
    print(f"Detected cycle? {has_cycle}")
    print(f"Cycle path: {cycle_path}")

    if has_cycle and cycle_path:
        # Choose a victim from the cycle (simple strategy)
        victim = cycle_path[1] if len(cycle_path) > 2 else cycle_path[0]
        print(f"\nRecovery strategy: kill {victim} (simulate process termination)")

        wfg2 = recover_by_killing_process(wfg, victim)
        has_cycle2, cycle_path2 = detect_cycle_wait_for_graph(wfg2)

        print(f"After recovery - cycle? {has_cycle2}")
        print(f"Cycle path: {cycle_path2}\n")


if __name__ == "__main__":
    run_demo()
