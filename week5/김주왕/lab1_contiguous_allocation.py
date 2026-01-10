## 2) Lab 1: 연속 메모리 할당 + 외부 단편화 + Compaction

# lab1_contiguous_allocation.py
"""
Lab 1) Contiguous Memory Allocation Simulation
- First Fit / Best Fit / Worst Fit
- External Fragmentation (holes become too small / scattered)
- Compaction (memory compression) demonstration

이 실습은 "연속 메모리 할당"이 왜 외부 단편화를 유발하는지,
그리고 Compaction(압축)이 어떤 효과가 있지만 왜 비싼지(오버헤드)를 체감하기 위한 시뮬레이터입니다.
"""

from dataclasses import dataclass
from typing import List, Optional, Tuple


@dataclass
class Segment:
    """
    메모리의 한 구간(segment)을 표현합니다.
    - start: 시작 주소(정수)
    - size: 구간 크기
    - pid: None 이면 빈 공간(hole), 아니면 해당 프로세스가 점유
    """
    start: int
    size: int
    pid: Optional[str] = None

    @property
    def end(self) -> int:
        """구간의 끝 주소(미포함)."""
        return self.start + self.size

    def is_free(self) -> bool:
        return self.pid is None


def merge_adjacent_free_segments(segments: List[Segment]) -> List[Segment]:
    """
    인접한 빈 구간(hole)들이 있으면 합쳐서(병합) 단편화를 조금 줄여줍니다.
    - 예: [free 0~10], [free 10~20] -> [free 0~20]
    """
    if not segments:
        return []

    segments = sorted(segments, key=lambda s: s.start)
    merged = [segments[0]]

    for seg in segments[1:]:
        last = merged[-1]
        # 둘 다 빈 공간이고, 서로 인접하면 합친다
        if last.is_free() and seg.is_free() and last.end == seg.start:
            last.size += seg.size
        else:
            merged.append(seg)

    return merged


def print_memory_map(segments: List[Segment], total_size: int) -> None:
    """
    현재 메모리 상태를 사람이 읽기 좋게 출력합니다.
    """
    print("\n[Memory Map]")
    print(f"Total Memory Size: {total_size}")
    for seg in sorted(segments, key=lambda s: s.start):
        owner = "FREE" if seg.is_free() else f"PID={seg.pid}"
        print(f"  {seg.start:4d} ~ {seg.end:4d}  (size={seg.size:4d})  [{owner}]")

    free_total = sum(s.size for s in segments if s.is_free())
    largest_hole = max((s.size for s in segments if s.is_free()), default=0)
    print(f"Free Total: {free_total}, Largest Hole: {largest_hole}")


def find_hole(segments: List[Segment], request_size: int, policy: str) -> Optional[int]:
    """
    빈 공간(hole)을 찾는 함수입니다.
    policy:
      - "first": First Fit
      - "best" : Best Fit
      - "worst": Worst Fit

    return:
      - hole의 index (segments 리스트에서의 위치)
      - 없으면 None
    """
    free_indices = [i for i, s in enumerate(segments) if s.is_free() and s.size >= request_size]
    if not free_indices:
        return None

    if policy == "first":
        # 앞에서부터 처음 맞는 hole 선택
        return free_indices[0]

    if policy == "best":
        # 가능한 hole 중 "가장 작은 hole" 선택
        return min(free_indices, key=lambda i: segments[i].size)

    if policy == "worst":
        # 가능한 hole 중 "가장 큰 hole" 선택
        return max(free_indices, key=lambda i: segments[i].size)

    raise ValueError(f"Unknown policy: {policy}")


def allocate(segments: List[Segment], pid: str, size: int, policy: str) -> bool:
    """
    연속 메모리 할당을 수행합니다.
    - 빈 구간(hole)을 찾아서 그 안에 프로세스를 배치합니다.
    - hole이 더 크면, (프로세스 segment + 남은 free segment)로 쪼갭니다.

    return:
      - 성공 True / 실패 False
    """
    idx = find_hole(segments, size, policy)
    if idx is None:
        print(f"\n[ALLOC FAIL] pid={pid}, size={size} (No suitable contiguous hole)")
        return False

    hole = segments[idx]
    print(f"\n[ALLOC] policy={policy}, pid={pid}, size={size} -> hole({hole.start}~{hole.end}, size={hole.size})")

    # 프로세스가 차지할 구간
    allocated = Segment(start=hole.start, size=size, pid=pid)

    # hole이 남는다면 남은 free 구간 생성
    remainder_size = hole.size - size
    new_segments = segments[:idx] + [allocated]
    if remainder_size > 0:
        remainder = Segment(start=hole.start + size, size=remainder_size, pid=None)
        new_segments.append(remainder)
    new_segments += segments[idx + 1 :]

    # 인접 free 구간 병합
    segments[:] = merge_adjacent_free_segments(new_segments)
    return True


def deallocate(segments: List[Segment], pid: str) -> bool:
    """
    프로세스를 종료(메모리 해제)시킵니다.
    - 해당 pid의 segment를 찾아 FREE로 바꾸고 인접 hole 병합.
    """
    found = False
    for seg in segments:
        if seg.pid == pid:
            seg.pid = None
            found = True

    if not found:
        print(f"\n[FREE FAIL] pid={pid} not found")
        return False

    print(f"\n[FREE] pid={pid}")
    segments[:] = merge_adjacent_free_segments(segments)
    return True


def compaction(segments: List[Segment], total_size: int) -> None:
    """
    Compaction(메모리 압축) 시뮬레이션:
    - 프로세스들을 메모리 앞쪽으로 "쭉" 밀어서 한 덩어리의 큰 hole을 만들기

    현실에서 compaction은:
    - 시스템이 하던 일을 멈출 수 있고,
    - 메모리 복사가 많아 오버헤드가 큽니다.
    여기서는 '효과'만 간단히 시뮬레이션합니다.
    """
    print("\n[COMPACTION] Move allocated segments to the front to create one big free hole.")

    allocated_segs = [s for s in segments if not s.is_free()]
    allocated_segs = sorted(allocated_segs, key=lambda s: s.start)

    # 앞에서부터 차곡차곡 붙인다
    new_segments: List[Segment] = []
    cursor = 0
    for seg in allocated_segs:
        new_segments.append(Segment(start=cursor, size=seg.size, pid=seg.pid))
        cursor += seg.size

    # 남은 영역은 하나의 큰 FREE hole
    if cursor < total_size:
        new_segments.append(Segment(start=cursor, size=total_size - cursor, pid=None))

    segments[:] = new_segments


def main():
    # 실험용 전체 메모리 크기
    total_size = 100

    # 초기 상태: 메모리 전체가 FREE 하나의 덩어리
    segments = [Segment(start=0, size=total_size, pid=None)]

    print("=== Lab 1: Contiguous Allocation + External Fragmentation + Compaction ===")

    # 1) 여러 프로세스를 할당하여 메모리를 채운다
    allocate(segments, "A", 18, "first")
    allocate(segments, "B", 12, "first")
    allocate(segments, "C", 15, "first")
    allocate(segments, "D", 10, "first")
    allocate(segments, "E", 8,  "first")
    print_memory_map(segments, total_size)

    # 2) 중간중간 프로세스를 종료시켜 hole을 만든다 (단편화 유발)
    deallocate(segments, "B")
    deallocate(segments, "D")
    print_memory_map(segments, total_size)

    # 3) 큰 프로세스를 올리려 시도 → total free는 있어도 "연속 hole"이 부족하면 실패 가능
    allocate(segments, "BIG", 25, "first")  # 외부 단편화 상황이면 실패할 수 있음
    print_memory_map(segments, total_size)

    # 4) Compaction 수행 후 다시 시도하면 성공할 가능성이 커짐
    compaction(segments, total_size)
    print_memory_map(segments, total_size)

    allocate(segments, "BIG", 25, "first")
    print_memory_map(segments, total_size)

    # 5) policy 비교: best/worst는 상황 따라 결과가 달라짐
    print("\n--- Policy comparison quick demo ---")
    segments2 = [Segment(start=0, size=total_size, pid=None)]
    allocate(segments2, "P1", 20, "first")
    allocate(segments2, "P2", 15, "first")
    allocate(segments2, "P3", 10, "first")
    deallocate(segments2, "P2")
    allocate(segments2, "X", 12, "best")   # best는 가장 작은 hole에 넣으려 함
    print_memory_map(segments2, total_size)


if __name__ == "__main__":
    main()
