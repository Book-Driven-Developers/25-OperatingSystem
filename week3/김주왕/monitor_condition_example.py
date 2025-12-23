import threading

condition = threading.Condition()
items = []

def producer():
    with condition:
        items.append("item")
        print("Producer produced item")
        condition.notify()

def consumer():
    with condition:
        while not items:
            condition.wait()
        print("Consumer consumed item:", items.pop())

t1 = threading.Thread(target=consumer)
t2 = threading.Thread(target=producer)

t1.start()
t2.start()

t1.join()
t2.join()
