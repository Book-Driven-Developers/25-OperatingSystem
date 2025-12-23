import threading
import time

semaphore = threading.Semaphore(0)

def producer():
    time.sleep(1)
    print("Producer: item produced")
    semaphore.release()

def consumer():
    print("Consumer: waiting for item...")
    semaphore.acquire()
    print("Consumer: item consumed")

t1 = threading.Thread(target=consumer)
t2 = threading.Thread(target=producer)

t1.start()
t2.start()

t1.join()
t2.join()
