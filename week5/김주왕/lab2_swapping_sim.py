# lab2_swapping_sim.py
"""
Lab 2) Swapping Simulation
- 메모리가 부족할 때, 어떤 프로세스를 "swap out"하여 swap 공간으로 내보내고
  새 프로세스를 "swap in"하여 올리는 과정을 시뮬레이션합니다.

중요한 포인트:
- Swap-out된 프로세스가 나중에 swap-in될 때는
  '예전과 다른 물리 위치(주소)'에 올라갈 수 있습니다.
- swap은 디스크 I/O 성격이라 현실에서는 매우 비쌉니다.
"""

from dataclasses import dataclass
from typing import Dict, List, Optional


@dataclass
class Process:
    pid: str
    size: int  # 메모리에서 필요한 크기(연속이라고 가정)
    state: str = "NEW"  # NEW / READY / RUNNING / SWAPPED


@dataclass
class Placement:
    """
    어떤 프로세스가 메모리의 어디에 올라가 있는지(물리 위치)를 기록합니다.
    """
    start: int
    size: int

    @property
    def end(self) -> int:
        return self.start + self.size


class SimpleMemory:
    """
    단순화를 위해 "연속 메모리"로 가정하고,
    빈 공간 관리 및 프로세스 적재/해제를 제공합니다.
    """
    def __init__(self, total_size: int):
        self.total_size = total_size
        # (start, size, pid) 형태의 세그먼트 리스트
        self.segments = [(0, total_size, None)]  # 처음엔 전부 free

    def _merge(self):
        """
        인접한 free 세그먼트들을 병합해서 단편화를 조금 줄입니다.
        """
        merged = []
        for start, size, pid in sorted(self.segments, key=lambda x: x[0]):
            if not merged:
                merged.append([start, size, pid])
                continue
            last = merged[-1]
            if last[2] is None and pid is None and last[0] + last[1] == start:
                last[1] += size
            else:
                merged.append([start, size, pid])
        self.segments = [(a, b, c) for a, b, c in merged]

    def allocate_first_fit(self, pid: str, size: int) -> Optional[Placement]:
        """
        First Fit로 연속 공간을 찾아 할당합니다.
        """
        for i, (start, seg_size, owner) in enumerate(self.segments):
            if owner is None and seg_size >= size:
                # 할당
                alloc = (start, size, pid)
                remainder = (start + size, seg_size - size, None)
                new_segs = self.segments[:i] + [alloc]
                if remainder[1] > 0:
                    new_segs.append(remainder)
                new_segs += self.segments[i+1:]
                self.segments = new_segs
                self._merge()
                return Placement(start=start, size=size)
        return None

    def free(self, pid: str) -> bool:
        """
        pid가 점유한 구간을 FREE로 바꿉니다.
        """
        changed = False
        new_segs = []
        for start, size, owner in self.segments:
            if owner == pid:
                new_segs.append((start, size, None))
                changed = True
            else:
                new_segs.append((start, size, owner))
        self.segments = new_segs
        self._merge()
        return changed

    def largest_hole(self) -> int:
        """
        가장 큰 빈 공간 크기를 반환합니다.
        """
        holes = [size for _, size, owner in self.segments if owner is None]
        return max(holes, default=0)

    def print_map(self):
        print("\n[Memory Map]")
        print(f"Total={self.total_size}")
        for start, size, owner in sorted(self.segments, key=lambda x: x[0]):
            end = start + size
            tag = "FREE" if owner is None else f"PID={owner}"
            print(f"  {start:4d} ~ {end:4d} (size={size:3d}) [{tag}]")

        free_total = sum(size for _, size, owner in self.segments if owner is None)
        print(f"Free Total={free_total}, Largest Hole={self.largest_hole()}")


class SwapSpace:
    """
    swap 영역을 단순히 '프로세스 저장소'처럼 취급합니다.
    """
    def __init__(self):
        self.store: Dict[str, Process] = {}

    def swap_out(self, p: Process):
        p.state = "SWAPPED"
        self.store[p.pid] = p

    def swap_in(self, pid: str) -> Optional[Process]:
        if pid not in self.store:
            return None
        p = self.store.pop(pid)
        p.state = "READY"
        return p

    def print_state(self):
        print("\n[Swap Space]")
        if not self.store:
            print("  (empty)")
            return
        for pid, p in self.store.items():
            print(f"  PID={pid}, size={p.size}, state={p.state}")


def main():
    print("=== Lab 2: Swapping Simulation ===")

    mem = SimpleMemory(total_size=50)
    swap = SwapSpace()

    # 프로세스들(크기 다양)
    processes = [
        Process("A", 18),
        Process("B", 12),
        Process("C", 14),
        Process("D", 10),
        Process("E", 8),
    ]

    placements: Dict[str, Placement] = {}

    # 1) A, B, C를 먼저 올린다
    for pid in ["A", "B", "C"]:
        p = next(x for x in processes if x.pid == pid)
        pl = mem.allocate_first_fit(p.pid, p.size)
        if pl:
            placements[p.pid] = pl
            p.state = "READY"
            print(f"[LOAD] PID={p.pid} loaded at {pl.start}~{pl.end}")
        else:
            print(f"[LOAD FAIL] PID={p.pid} cannot be loaded")

    mem.print_map()
    swap.print_state()

    # 2) 이제 D를 올리려는데 공간 부족이면 swap-out을 실행해본다
    print("\n--- Try to load D (may need swapping) ---")
    pD = next(x for x in processes if x.pid == "D")
    plD = mem.allocate_first_fit(pD.pid, pD.size)

    if plD is None:
        print("[NO SPACE] Need to swap out someone to load D")

        # 매우 단순한 정책: "가장 오래 메모리에 있었던 프로세스"를 swap out한다고 가정
        victim_pid = "A"
        victim = next(x for x in processes if x.pid == victim_pid)

        # swap out: 메모리에서 내리고 swap에 넣는다
        mem.free(victim_pid)
        swap.swap_out(victim)
        placements.pop(victim_pid, None)
        print(f"[SWAP OUT] PID={victim_pid} moved to swap space")

        # 이제 다시 D 로드 시도
        plD = mem.allocate_first_fit(pD.pid, pD.size)

    if plD:
        placements[pD.pid] = plD
        pD.state = "READY"
        print(f"[LOAD] PID={pD.pid} loaded at {plD.start}~{plD.end}")

    mem.print_map()
    swap.print_state()

    # 3) 나중에 A가 다시 필요해져서 swap-in 한다고 가정
    print("\n--- Swap-in A again (address may change) ---")
    pA = swap.swap_in("A")
    if pA is None:
        print("[SWAP IN FAIL] A not in swap")
    else:
        # swap-in된 프로세스는 예전 물리 위치가 아니라 "현재 빈 곳"에 올라감
        plA_new = mem.allocate_first_fit(pA.pid, pA.size)
        if plA_new is None:
            print("[NO SPACE] Still no space to swap-in A")
            # (현실에서는 또 다른 프로세스 swap-out 필요)
        else:
            placements[pA.pid] = plA_new
            print(f"[SWAP IN] PID=A loaded at {plA_new.start}~{plA_new.end} (may differ from old address!)")

    mem.print_map()
    swap.print_state()

    # 요약
    print("\n[Summary]")
    print("- Swap-out: Memory -> Swap space")
    print("- Swap-in : Swap space -> Memory (can be loaded at a different physical location)")


if __name__ == "__main__":
    main()
