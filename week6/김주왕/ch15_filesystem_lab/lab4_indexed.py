"""Lab 4: Indexed allocation (simple inode simulation)

- Each file has an inode with direct pointers and one single indirect block
- The indirect block stores a list of block indices
- Demonstrates direct vs indirect access
"""
from block_device import BlockDevice

class Inode:
    def __init__(self, direct_count=4):
        self.direct = []
        self.indirect = None
        self.direct_count = direct_count

class IndexedFS:
    def __init__(self, device, direct_count=4):
        self.dev = device
        self.inodes = {}  # name -> Inode
        self.direct_count = direct_count

    def allocate(self, name, length):
        inode = Inode(self.direct_count)
        # allocate direct first
        d = min(length, self.direct_count)
        blocks = self.dev.allocate_blocks(d)
        if blocks is None:
            return False
        inode.direct = blocks
        remaining = length - d
        if remaining > 0:
            indirect_blocks = self.dev.allocate_blocks(1)
            if indirect_blocks is None:
                # rollback
                self.dev.free_blocks(blocks)
                return False
            # allocate the actual data blocks for indirect list
            data_blocks = self.dev.allocate_blocks(remaining)
            if data_blocks is None:
                self.dev.free_blocks(blocks + indirect_blocks)
                return False
            inode.indirect = (indirect_blocks[0], data_blocks)
            # store pointers in the indirect block (simulated)
            self.dev.write_block(indirect_blocks[0], f"indirect:{data_blocks}")
            for i, b in enumerate(data_blocks):
                self.dev.write_block(b, f"{name}:indirect:{i}")
        for i, b in enumerate(inode.direct):
            self.dev.write_block(b, f"{name}:direct:{i}")
        self.inodes[name]=inode
        return True

    def delete(self, name):
        if name not in self.inodes:
            return False
        inode=self.inodes.pop(name)
        self.dev.free_blocks(inode.direct)
        if inode.indirect:
            idx, data_blocks = inode.indirect
            self.dev.free_blocks([idx])
            self.dev.free_blocks(data_blocks)
        return True

if __name__=='__main__':
    dev=BlockDevice(num_blocks=64)
    fs=IndexedFS(dev,direct_count=4)
    print('Initial', dev.dump_state())
    print('Alloc A(3)->', fs.allocate('A',3))
    print('Alloc B(10)->', fs.allocate('B',10))
    print('State', dev.dump_state())
    print('Delete B->', fs.delete('B'))
    print('Final', dev.dump_state())
