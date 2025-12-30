# -*- coding: utf-8 -*-
"""
dining_prevent_ordered.py
-------------------------
Deadlock prevention demo (break circular wait).

Technique:
- Impose a global ordering on resources (fork IDs).
- Always acquire the lower-ID fork first, then the higher-ID fork.
- This prevents circular wait, so deadlock cannot happen.
"""

from __future__ import annotations
import threading
import time
from common import banner, sleep_seconds


def run(n: int = 5, run_seconds: int = 6) -> None:
    forks = [threading.Lock() for _ in range(n)]
    stop_event = threading.Event()

    def philosopher(i: int) -> None:
        a = i
        b = (i + 1) % n
        first = min(a, b)
        second = max(a, b)

        while not stop_event.is_set():
            time.sleep(0.05)

            forks[first].acquire()
            print(f"[P{i}] took fork {first} (first)")

            time.sleep(0.02)

            forks[second].acquire()
            print(f"[P{i}] took fork {second} (second) => EATING")

            time.sleep(0.03)

            forks[second].release()
            forks[first].release()
            print(f"[P{i}] released forks {first},{second}")

    threads = [
        threading.Thread(target=philosopher, args=(i,), daemon=True)
        for i in range(n)
    ]
    for t in threads:
        t.start()

    banner("RUN: Prevention (Ordered locking). Should keep making progress.")
    sleep_seconds(run_seconds)

    stop_event.set()
    time.sleep(0.2)
    banner("STOP: Prevention demo finished.")


if __name__ == "__main__":
    run()
