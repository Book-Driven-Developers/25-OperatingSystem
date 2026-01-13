"""Lab 1: Contiguous allocation simulation

Strategy:
- allocate a single contiguous run of `k` blocks
- implement simple allocation by scanning for a run of free blocks
- show fragmentation effect by allocating/freeing files
"""
from block_device import BlockDevice

class ContiguousFS:
    def __init__(self, device):
        self.dev = device
        self.files = {}  # name -> (start, length)

    def allocate(self, name, length):
        # find run of `length` free blocks
        n = self.dev.num_blocks
        run = 0
        start = None
        for i in range(n):
            if self.dev.free[i]:
                run += 1
            else:
                run = 0
            if run == length:
                start = i - length + 1
                break
        if start is None:
            return False
        # mark allocated
        for j in range(start, start+length):
            self.dev.free[j] = False
            self.dev.write_block(j, f"{name}:{j-start}")
        self.files[name] = (start, length)
        return True

    def delete(self, name):
        if name not in self.files:
            return False
        start, length = self.files.pop(name)
        for j in range(start, start+length):
            self.dev.free[j] = True
            self.dev.write_block(j, None)
        return True

if __name__=='__main__':
    dev = BlockDevice(num_blocks=64)
    fs = ContiguousFS(dev)
    print('Initial', dev.dump_state())
    print('Allocating file A (10) ->', fs.allocate('A', 10))
    print('Allocating file B (15) ->', fs.allocate('B', 15))
    print('Allocating file C (8) ->', fs.allocate('C', 8))
    print('State after allocs', dev.dump_state())
    print('Delete B ->', fs.delete('B'))
    print('State after free B', dev.dump_state())
    print('Allocating file D (12) ->', fs.allocate('D', 12))
    print('Final', dev.dump_state())
