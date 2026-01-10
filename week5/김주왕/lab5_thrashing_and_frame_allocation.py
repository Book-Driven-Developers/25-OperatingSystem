# lab5_thrashing_and_frame_allocation.py
"""
Lab 5) Thrashing & Frame Allocation (Working Set concept)
- 프레임 수가 너무 적으면 페이지 폴트가 폭발적으로 늘고,
  CPU가 실제 작업보다 페이징(교체)에 더 많은 시간을 쓰게 되는 현상을 "스래싱"이라고 합니다.

이 실습에서는:
1) 특정 참조열(reference string)을 준비하고
2) 프레임 수를 1~N까지 바꿔가며 페이지 폴트를 측정
3) "프레임을 조금만 늘려도 페이지 폴트가 급감하는 임계점"을 관찰합니다.

추가로:
- Working Set(작업 집합) 아이디어를 단순화해서,
  일정 window 내에서 참조되는 서로 다른 페이지 수를 계산해봅니다.
  (작업 집합 크기 ~= 그 프로세스가 안정적으로 돌아가려면 필요한 프레임 수)
"""

from collections import OrderedDict
from typing import List, Set, Tuple


def lru_fault_count(pages: List[int], frame_size: int) -> int:
    """
    LRU 기준으로 page fault 개수를 센다.
    - frame_size가 작을수록 보통 fault가 커짐
    """
    frames = OrderedDict()
    faults = 0

    for p in pages:
        if p in frames:
            frames.move_to_end(p)  # 최근 사용 갱신
        else:
            faults += 1
            if len(frames) >= frame_size:
                frames.popitem(last=False)  # LRU 제거
            frames[p] = True
    return faults


def working_set_sizes(pages: List[int], window: int) -> List[int]:
    """
    작업 집합(Working Set)을 아주 단순 모델로 계산:
    - window 길이만큼 "최근 참조"를 보고,
    - 그 안에 등장한 서로 다른 페이지 개수 = working set size

    예) window=5이면, 현재 시점 기준 최근 5개 참조에서 서로 다른 페이지 개수를 본다.
    """
    sizes = []
    for i in range(len(pages)):
        start = max(0, i - window + 1)
        recent = pages[start:i+1]
        sizes.append(len(set(recent)))
    return sizes


def simulate_thrashing(pages: List[int], max_frames: int) -> None:
    """
    프레임 수를 1..max_frames 로 바꿔가며 fault를 측정해 출력.
    """
    print("\n[Thrashing Experiment: faults by frame size (LRU)]")
    for f in range(1, max_frames + 1):
        faults = lru_fault_count(pages, f)
        print(f"  frames={f:2d} -> page_faults={faults:3d}")


def main():
    print("=== Lab 5: Thrashing & Frame Allocation ===")

    # locality가 있는 참조열을 예로 듦
    # - (1,2,3) 주변을 자주 돌다가
    # - 갑자기 (7,8,9)로 이동
    # - 다시 (1,2,3)으로 돌아오는 형태
    reference = [
        1,2,3, 1,2,3, 1,2,3,
        4,1,2,3, 2,1,
        7,8,9, 7,8,9, 7,8,
        1,2,3, 1,2,3
    ]

    print(f"Reference length = {len(reference)}")
    print(f"Reference string = {reference}")

    # 1) 프레임 수 변화에 따른 페이지 폴트 관찰
    simulate_thrashing(reference, max_frames=8)

    # 2) 작업 집합(Working Set) 크기 관찰
    # window를 5, 10으로 바꿔보면 “이 프로세스가 최근에 어느 정도 페이지 집합을 쓰는지” 느낌이 옴
    for window in [5, 10]:
        ws = working_set_sizes(reference, window=window)
        print(f"\n[Working Set Size] window={window}")
        print("  index: working_set_size")
        for i, s in enumerate(ws):
            print(f"  {i:2d}: {s}")

    # 3) 해석 가이드
    print("\n[Interpretation Guide]")
    print("- working set size가 예를 들어 3~4 수준이라면, 프레임을 그 정도 이상 주면 fault가 안정화되는 경향")
    print("- 프레임이 working set보다 훨씬 작으면, 계속 밀어내고 다시 불러오며 fault 폭증(스래싱 위험)")
    print("- 실제 OS는 PFF(Page Fault Frequency), Working Set model 등으로 동적 프레임 할당을 시도")


if __name__ == "__main__":
    main()
