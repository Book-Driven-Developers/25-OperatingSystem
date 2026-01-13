"""Lab 2: Linked (chained) allocation simulation

- Each file stores a chain of block indices
- Allocation picks free blocks and links them
- No contiguous requirement; random access requires following chain
"""
from block_device import BlockDevice

class LinkedFS:
    def __init__(self, device):
        self.dev = device
        self.files = {}  # name -> list(blocks)

    def allocate(self, name, length):
        blocks = self.dev.allocate_blocks(length)
        if blocks is None:
            return False
        # write simple pointers (simulated by storing order)
        for i, b in enumerate(blocks):
            self.dev.write_block(b, f"{name}:{i}")
        self.files[name] = blocks
        return True

    def delete(self, name):
        if name not in self.files:
            return False
        self.dev.free_blocks(self.files.pop(name))
        return True

if __name__=='__main__':
    dev = BlockDevice(num_blocks=64)
    fs = LinkedFS(dev)
    print('Initial', dev.dump_state())
    print('Alloc A(10) ->', fs.allocate('A',10))
    print('Alloc B(20) ->', fs.allocate('B',20))
    print('Delete A ->', fs.delete('A'))
    print('Alloc C(12) ->', fs.allocate('C',12))
    print('Final', dev.dump_state())
