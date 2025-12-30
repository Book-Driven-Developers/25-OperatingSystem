# threading_practice_lab.py
# ------------------------------------------------------------
# Threading practice lab (standard library only)
#
# Topics:
# 1) Create & start threads
# 2) join(): wait for threads
# 3) daemon thread
# 4) Lock: protect critical section
# 5) RLock: re-entrant lock (same thread can acquire multiple times)
# 6) Semaphore: limit concurrency
# 7) Event: one-to-many signal
# 8) Condition: producer/consumer coordination
# 9) Queue: thread-safe work queue
# 10) Deadlock demo (two locks, opposite order)
#
# Run:
#   python threading_practice_lab.py
# ------------------------------------------------------------

import threading
import time
import queue


def banner(title: str) -> None:
    print("\n" + "=" * 72)
    print(title)
    print("=" * 72)


# -------------------------------------------------------------------
# 1) Create & start threads
# -------------------------------------------------------------------
def demo_create_start():
    banner("1) Create & start threads")

    def worker(name: str):
        for i in range(3):
            print(f"[{name}] step {i}")
            time.sleep(0.2)

    t1 = threading.Thread(target=worker, args=("T1",))
    t2 = threading.Thread(target=worker, args=("T2",))

    print("Starting threads...")
    t1.start()
    t2.start()

    print("Main thread continues immediately (no join yet).")
    time.sleep(1.0)
    print("Main done (threads may still be running).")


# -------------------------------------------------------------------
# 2) join(): wait for threads
# -------------------------------------------------------------------
def demo_join():
    banner("2) join(): wait for threads")

    def worker(name: str):
        time.sleep(0.6)
        print(f"[{name}] finished")

    t1 = threading.Thread(target=worker, args=("T1",))
    t2 = threading.Thread(target=worker, args=("T2",))

    start = time.time()
    t1.start()
    t2.start()

    print("Main waits for T1 and T2 by join()...")
    t1.join()
    t2.join()

    elapsed = time.time() - start
    print(f"All done. elapsed={elapsed:.2f}s")


# -------------------------------------------------------------------
# 3) daemon thread
# -------------------------------------------------------------------
def demo_daemon():
    banner("3) daemon thread")

    def background():
        i = 0
        while True:
            print(f"[daemon] heartbeat {i}")
            i += 1
            time.sleep(0.3)

    t = threading.Thread(target=background, daemon=True)
    t.start()

    print("Daemon thread started. Main will exit in ~1.2s.")
    time.sleep(1.2)
    print("Main exiting now -> daemon stops automatically.")


# -------------------------------------------------------------------
# 4) Lock: protect critical section
# -------------------------------------------------------------------
def demo_lock():
    banner("4) Lock: protect critical section")

    counter = 0
    lock = threading.Lock()

    def worker():
        nonlocal counter
        for _ in range(200_000):
            # Critical section
            with lock:
                counter += 1

    t1 = threading.Thread(target=worker)
    t2 = threading.Thread(target=worker)

    t1.start()
    t2.start()
    t1.join()
    t2.join()

    print("Expected counter = 400000")
    print("Actual counter   =", counter)


# -------------------------------------------------------------------
# 5) RLock: re-entrant lock
# -------------------------------------------------------------------
def demo_rlock():
    banner("5) RLock: re-entrant lock")

    rlock = threading.RLock()

    def inner():
        # Same thread can acquire the same RLock again
        with rlock:
            print("  inner() acquired rlock")

    def outer():
        with rlock:
            print("outer() acquired rlock")
            inner()
            print("outer() releasing rlock")

    outer()
    print("If you used Lock instead of RLock here, inner() would deadlock.")


# -------------------------------------------------------------------
# 6) Semaphore: limit concurrency
# -------------------------------------------------------------------
def demo_semaphore():
    banner("6) Semaphore: limit concurrency")

    sem = threading.Semaphore(2)  # allow only 2 threads in at a time

    def worker(i: int):
        print(f"[W{i}] waiting for semaphore...")
        with sem:
            print(f"[W{i}] ENTER (only 2 can be here)")
            time.sleep(0.8)
            print(f"[W{i}] EXIT")

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(5)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()


# -------------------------------------------------------------------
# 7) Event: one-to-many signal
# -------------------------------------------------------------------
def demo_event():
    banner("7) Event: one-to-many signal")

    start_event = threading.Event()

    def worker(i: int):
        print(f"[W{i}] waiting for event...")
        start_event.wait()  # block until event is set
        print(f"[W{i}] started work!")
        time.sleep(0.3)
        print(f"[W{i}] done")

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(3)]
    for t in threads:
        t.start()

    time.sleep(1.0)
    print("Main: set event -> all workers start together")
    start_event.set()

    for t in threads:
        t.join()


# -------------------------------------------------------------------
# 8) Condition: producer/consumer coordination
# -------------------------------------------------------------------
def demo_condition():
    banner("8) Condition: producer/consumer coordination")

    cond = threading.Condition()
    buffer = []
    MAX_ITEMS = 5
    PRODUCE_COUNT = 8

    def producer():
        for x in range(PRODUCE_COUNT):
            time.sleep(0.2)
            with cond:
                while len(buffer) >= MAX_ITEMS:
                    cond.wait()  # wait until consumer pops
                buffer.append(x)
                print(f"[producer] produced {x} | buffer={buffer}")
                cond.notify()  # wake consumer

    def consumer():
        consumed = 0
        while consumed < PRODUCE_COUNT:
            with cond:
                while not buffer:
                    cond.wait()  # wait until producer pushes
                item = buffer.pop(0)
                consumed += 1
                print(f"  [consumer] consumed {item} | buffer={buffer}")
                cond.notify()  # wake producer
            time.sleep(0.35)

    tp = threading.Thread(target=producer)
    tc = threading.Thread(target=consumer)
    tp.start()
    tc.start()
    tp.join()
    tc.join()
    print("Producer/Consumer done.")


# -------------------------------------------------------------------
# 9) Queue: thread-safe work queue
# -------------------------------------------------------------------
def demo_queue():
    banner("9) Queue: thread-safe work queue")

    q = queue.Queue()

    def worker(name: str):
        while True:
            item = q.get()
            if item is None:
                q.task_done()
                print(f"[{name}] received stop signal")
                return
            print(f"[{name}] processing {item}")
            time.sleep(0.2)
            q.task_done()

    workers = [threading.Thread(target=worker, args=(f"W{i}",)) for i in range(3)]
    for t in workers:
        t.start()

    # Enqueue tasks
    for x in range(10):
        q.put(x)

    # Stop signals
    for _ in workers:
        q.put(None)

    q.join()
    for t in workers:
        t.join()
    print("Queue demo done.")


# -------------------------------------------------------------------
# 10) Deadlock demo (opposite lock order)
# -------------------------------------------------------------------
def demo_deadlock():
    banner("10) Deadlock demo (Ctrl+C to stop)")

    lock_a = threading.Lock()
    lock_b = threading.Lock()

    def t1():
        print("T1: acquire A")
        lock_a.acquire()
        print("T1: acquired A")
        time.sleep(0.2)
        print("T1: acquire B (will wait if T2 holds B)")
        lock_b.acquire()
        print("T1: acquired B")  # likely never printed
        lock_b.release()
        lock_a.release()

    def t2():
        print("T2: acquire B")
        lock_b.acquire()
        print("T2: acquired B")
        time.sleep(0.2)
        print("T2: acquire A (will wait if T1 holds A)")
        lock_a.acquire()
        print("T2: acquired A")  # likely never printed
        lock_a.release()
        lock_b.release()

    threading.Thread(target=t1, daemon=True).start()
    threading.Thread(target=t2, daemon=True).start()

    print("If deadlock happens, you will see both threads 'waiting' forever.")
    while True:
        time.sleep(1)


# -------------------------------------------------------------------
# Menu
# -------------------------------------------------------------------
def main():
    menu = """
==============================
Threading Practice Lab
==============================
1) Create & start threads
2) join()
3) daemon thread
4) Lock (critical section)
5) RLock (re-entrant)
6) Semaphore (limit concurrency)
7) Event (signal)
8) Condition (coordination)
9) Queue (work queue)
10) Deadlock demo (Ctrl+C to stop)
0) Exit
Select> """
    actions = {
        "1": demo_create_start,
        "2": demo_join,
        "3": demo_daemon,
        "4": demo_lock,
        "5": demo_rlock,
        "6": demo_semaphore,
        "7": demo_event,
        "8": demo_condition,
        "9": demo_queue,
        "10": demo_deadlock,
    }

    while True:
        choice = input(menu).strip()
        if choice == "0":
            print("Bye.")
            return
        action = actions.get(choice)
        if not action:
            print("Invalid selection.")
            continue
        action()


if __name__ == "__main__":
    main()
