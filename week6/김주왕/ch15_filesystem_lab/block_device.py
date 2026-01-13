"""Block device simulator

- fixed number of blocks
- each block is an integer index; data is simulated as strings
- manage free block bitmap
"""

class BlockDevice:
    def __init__(self, num_blocks=1024):
        self.num_blocks = num_blocks
        self.blocks = [None] * num_blocks
        self.free = [True] * num_blocks

    def allocate_blocks(self, count):
        """Allocate `count` free blocks (first-fit) and return indices list."""
        out = []
        for i in range(self.num_blocks):
            if self.free[i]:
                out.append(i)
                self.free[i] = False
                if len(out) == count:
                    break
        if len(out) < count:
            # rollback
            for idx in out:
                self.free[idx] = True
            return None
        return out

    def free_blocks(self, indices):
        for i in indices:
            if 0 <= i < self.num_blocks:
                self.blocks[i] = None
                self.free[i] = True

    def write_block(self, idx, data):
        self.blocks[idx] = data

    def read_block(self, idx):
        return self.blocks[idx]

    def free_count(self):
        return sum(1 for x in self.free if x)

    def dump_state(self, max_show=64):
        used = [i for i,v in enumerate(self.free) if not v]
        return f"num_blocks={self.num_blocks} used={len(used)} free={self.free_count()} sample_used={used[:max_show]}" 
