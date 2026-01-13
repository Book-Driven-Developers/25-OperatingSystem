"""Lab 5: Simple inode with multi-level indirect demonstration

- Show how direct + single indirect can be used
- This is a pedagogical example; not a full filesystem
"""
from block_device import BlockDevice

# Reuse IndexedFS for simplicity
from lab4_indexed import IndexedFS

if __name__=='__main__':
    dev=BlockDevice(num_blocks=128)
    fs=IndexedFS(dev,direct_count=6)
    print('Initial', dev.dump_state())
    print('Alloc LargeFile(20)->', fs.allocate('LargeFile',20))
    print('Inspect state', dev.dump_state())
    print('Delete LargeFile->', fs.delete('LargeFile'))
    print('Final', dev.dump_state())
