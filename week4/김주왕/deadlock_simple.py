# deadlock_without_threading.py
# ------------------------------------------------------------
# Deadlock demo WITHOUT threading.
# We simulate concurrency using generator-based "tasks"
# and a simple round-robin scheduler.
# ------------------------------------------------------------

class Lock:
    def __init__(self, name: str):
        self.name = name
        self.owner = None  # which task holds this lock (string)

    def acquire(self, who: str) -> bool:
        # Non-preemptive: if already held by someone else, acquisition fails
        if self.owner is None:
            self.owner = who
            return True
        return False

    def release(self, who: str) -> None:
        if self.owner == who:
            self.owner = None

    def __repr__(self) -> str:
        return f"Lock({self.name}, owner={self.owner})"


def task_A(lock1: Lock, lock2: Lock):
    name = "A"

    # 1) acquire lock1
    while not lock1.acquire(name):
        print(f"[{name}] waiting for {lock1.name}")
        yield  # give up CPU
    print(f"[{name}] acquired {lock1.name}")
    yield

    # 2) acquire lock2 (this is where deadlock can happen)
    while not lock2.acquire(name):
        print(f"[{name}] holding {lock1.name}, waiting for {lock2.name}")
        yield
    print(f"[{name}] acquired {lock2.name}")
    yield

    # 3) critical section done
    lock2.release(name)
    lock1.release(name)
    print(f"[{name}] released {lock2.name}, {lock1.name}")


def task_B(lock1: Lock, lock2: Lock):
    name = "B"

    # NOTE: opposite order (lock2 -> lock1)
    while not lock2.acquire(name):
        print(f"[{name}] waiting for {lock2.name}")
        yield
    print(f"[{name}] acquired {lock2.name}")
    yield

    while not lock1.acquire(name):
        print(f"[{name}] holding {lock2.name}, waiting for {lock1.name}")
        yield
    print(f"[{name}] acquired {lock1.name}")
    yield

    lock1.release(name)
    lock2.release(name)
    print(f"[{name}] released {lock1.name}, {lock2.name}")


def run_scheduler(tasks, max_steps: int = 50):
    """
    Round-robin scheduler.
    If in a full round nobody makes progress, we declare DEADLOCK.
    (progress = a task advances at least one yield)
    """
    alive = tasks[:]

    for step in range(max_steps):
        if not alive:
            print("\nAll tasks finished (no deadlock).")
            return

        progressed_this_round = False
        next_alive = []

        for t in alive:
            try:
                # Advance the task by one "time slice"
                next(t)
                progressed_this_round = True
                next_alive.append(t)
            except StopIteration:
                # Task finished
                pass

        alive = next_alive

        # If tasks are still alive but none progressed, deadlock.
        if alive and not progressed_this_round:
            print("\nDEADLOCK detected: all remaining tasks are stuck.")
            return

    print("\nStopped (max steps reached). Might be deadlock or just long-running.")


def main():
    lockA = Lock("L1")
    lockB = Lock("L2")

    # A: L1 -> L2
    # B: L2 -> L1  (opposite order => circular wait)
    tA = task_A(lockA, lockB)
    tB = task_B(lockA, lockB)

    print("Initial locks:", lockA, lockB, "\n")
    run_scheduler([tA, tB], max_steps=30)
    print("\nFinal locks:", lockA, lockB)


if __name__ == "__main__":
    main()
