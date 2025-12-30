# dining_deadlock.py
# ---------------------------------------
# Created by GPT based on user instructions.
# Dining Philosophers: intentionally cause deadlock
#
# Idea:
#   Every philosopher does:
#     1) pick LEFT fork
#     2) wait a bit (so everyone can pick their LEFT fork)
#     3) try to pick RIGHT fork
#
# If all philosophers hold LEFT and wait for RIGHT,
# we get a circular wait -> deadlock.
# ---------------------------------------

import threading
import time


def philosopher(i, forks, stop):
    """
    A philosopher is a thread.
    forks[k] is a Lock for fork k.
    """
    left, right = i, (i + 1) % len(forks)

    while not stop.is_set():
        # 1) Think
        time.sleep(0.1)

        # 2) Pick LEFT fork first (deadlock-prone pattern)
        if not forks[left].acquire(timeout=0.1):
            continue

        try:
            # 3) Make deadlock more likely:
            #    give other philosophers time to pick their LEFT forks too
            time.sleep(0.2)

            # 4) Try to pick RIGHT fork (may block forever)
            if not forks[right].acquire(timeout=0.1):
                continue

            try:
                # If we reach here, we successfully got both forks (no deadlock for this round)
                print(f"P{i} eating")
                time.sleep(0.1)
            finally:
                forks[right].release()
        finally:
            forks[left].release()


def main():
    n = 5  # number of philosophers / forks
    forks = [threading.Lock() for _ in range(n)] # 포크는 한 순간에 한 철학자만 들 수 있어야 하니까(=상호배제), Lock이 딱 맞는 모델
    stop = threading.Event()
    threads = [] # to keep track of threads

    # Start N philosopher threads
    for i in range(n): # for each philosopher
        t = threading.Thread(target=philosopher, args=(i, forks, stop), daemon=False) # 데몬 스레드 아님: 데몬 스레드는 메인 스레드가 종료되면 강제 종료되는데, 여기선 데드락 상황을 관찰하려고 하니까 강제 종료되면 안 됨
        t.start(); threads.append(t)

    print("\nRunning... (press Ctrl+C to stop)")
    print("If deadlock happens, output will eventually stop.\n")

    try:
        for t in threads: # 메인 스레드가 종료되지 않도록 각 철학자 스레드가 종료될 때까지 기다림
            t.join()
    except KeyboardInterrupt:
        print("Interrupted — shutting down")
        stop.set()
        for t in threads:
            t.join(timeout=1)


if __name__ == "__main__":
    main()
