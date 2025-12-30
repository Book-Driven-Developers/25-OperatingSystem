# -*- coding: utf-8 -*-
"""
dining_deadlock.py
------------------
Dining Philosophers - induce deadlock intentionally.

Goal:
- Demonstrate deadlock by making every philosopher acquire LEFT fork first,
  then try to acquire RIGHT fork.
- With a small delay, circular wait is likely to happen.
"""

from __future__ import annotations
import threading
import time
from common import banner, sleep_seconds


def run(n: int = 5, run_seconds: int = 8) -> None:
    forks = [threading.Lock() for _ in range(n)]
    stop_event = threading.Event()

    def philosopher(i: int) -> None:
        left = i
        right = (i + 1) % n

        while not stop_event.is_set():
            # Think
            time.sleep(0.05)

            # Acquire LEFT fork first (this is the classic deadlock-prone pattern)
            forks[left].acquire()
            print(f"[P{i}] took LEFT fork {left}")

            # Delay to increase chance that everyone holds their LEFT fork
            time.sleep(0.10)

            # Acquire RIGHT fork (may block forever due to circular wait)
            forks[right].acquire()
            print(f"[P{i}] took RIGHT fork {right} => EATING")

            # Eat
            time.sleep(0.05)

            forks[right].release()
            forks[left].release()
            print(f"[P{i}] released forks {left},{right}")

    threads = [
        threading.Thread(target=philosopher, args=(i,), daemon=True)
        for i in range(n)
    ]
    for t in threads:
        t.start()

    banner("RUN: Dining Philosophers (Deadlock likely). Watch for freezing/no progress.")
    sleep_seconds(run_seconds)

    # Note: if a deadlock happens, threads blocked on acquire() won't exit cleanly.
    stop_event.set()
    time.sleep(0.2)
    banner("STOP requested. If deadlock occurred, program exit will terminate daemon threads.")


if __name__ == "__main__":
    run()
