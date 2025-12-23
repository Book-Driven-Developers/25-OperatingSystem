import threading

counter = 0

def increase():
    global counter
    for _ in range(100000):
        counter += 1

threads = []
for _ in range(2):
    t = threading.Thread(target=increase)
    threads.append(t)
    t.start()

for t in threads:
    t.join()

print("Final counter:", counter)
