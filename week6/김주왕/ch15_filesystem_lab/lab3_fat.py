"""Lab 3: FAT allocation simulation

- Simulate a FAT table (list of next indices, -1=end, None=free)
- Directory maps name -> first cluster
- Allocation fills FAT entries and data blocks
"""
from block_device import BlockDevice

class FATFS:
    def __init__(self, device):
        self.dev = device
        self.fat = [None] * device.num_blocks
        self.dir = {}  # name -> start_idx

    def allocate(self, name, length):
        blocks = self.dev.allocate_blocks(length)
        if blocks is None:
            return False
        for i in range(len(blocks)):
            idx=blocks[i]
            nxt = blocks[i+1] if i+1 < len(blocks) else -1
            self.fat[idx]=nxt
            self.dev.write_block(idx, f"{name}:{i}")
        self.dir[name]=blocks[0]
        return True

    def traverse(self, name):
        if name not in self.dir:
            return []
        idx=self.dir[name]
        out=[]
        while idx!=-1:
            out.append(idx)
            idx=self.fat[idx]
        return out

    def delete(self, name):
        blocks=self.traverse(name)
        for b in blocks:
            self.fat[b]=None
        self.dev.free_blocks(blocks)
        if name in self.dir:
            del self.dir[name]
        return True

if __name__=='__main__':
    dev=BlockDevice(num_blocks=64)
    fs=FATFS(dev)
    print('Initial', dev.dump_state())
    print('Alloc A(8)->', fs.allocate('A',8))
    print('Alloc B(12)->', fs.allocate('B',12))
    print('Traverse B', fs.traverse('B')[:10])
    print('Delete A->', fs.delete('A'))
    print('Alloc C(10)->', fs.allocate('C',10))
    print('Final', dev.dump_state())
