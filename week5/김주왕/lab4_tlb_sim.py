# lab4_tlb_sim.py
"""
Lab 4) TLB (Translation Lookaside Buffer) Simulation
- TLB는 '페이지 테이블의 일부를 캐시'하여 주소 변환 속도를 높입니다.
- TLB hit이면 페이지 테이블을 메모리에서 다시 찾지 않아도 되어 빠릅니다.
- TLB miss이면 페이지 테이블을 메모리에서 확인해야 합니다.

이 실습에서는:
- LRU 기반 TLB를 구현
- 참조열(reference string)에 대해 hit/miss를 측정
- 평균 접근 비용(간단 모델)을 계산합니다.
"""

from collections import OrderedDict
from dataclasses import dataclass
from typing import Dict, List, Tuple


@dataclass
class PTE:
    frame: int
    valid: bool = True


class TLBSimulator:
    """
    단순 TLB(LRU) 시뮬레이터

    비용 모델(아주 단순화):
    - TLB hit  : 비용 = 1 (TLB 조회 + 바로 frame 접근했다고 가정)
    - TLB miss : 비용 = 2 (페이지 테이블 접근 1 + frame 접근 1)
    """
    def __init__(self, tlb_capacity: int, page_table: Dict[int, PTE]):
        self.tlb_capacity = tlb_capacity
        self.page_table = page_table

        # LRU 캐시처럼 쓰기 위해 OrderedDict 사용
        # key=page_number, value=frame_number
        self.tlb = OrderedDict()

        self.hits = 0
        self.misses = 0
        self.total_cost = 0

    def access(self, page_number: int) -> int:
        """
        특정 page_number에 대해 TLB 접근을 수행합니다.
        return: frame_number
        """
        # 1) TLB에 있으면 hit
        if page_number in self.tlb:
            self.hits += 1
            self.total_cost += 1
            frame = self.tlb[page_number]
            # LRU 갱신: 최근 사용 항목을 끝으로 이동
            self.tlb.move_to_end(page_number)
            print(f"TLB HIT  - page {page_number} -> frame {frame} | TLB={list(self.tlb.keys())}")
            return frame

        # 2) 없으면 miss -> 페이지 테이블 확인
        self.misses += 1
        self.total_cost += 2

        pte = self.page_table.get(page_number)
        if pte is None or not pte.valid:
            # 이 실습에서는 page fault까지는 깊게 안 가고, 없으면 -1로 처리
            print(f"TLB MISS - page {page_number} -> PAGE FAULT (no valid PTE)")
            return -1

        frame = pte.frame

        # 3) TLB에 삽입 (capacity 초과 시 LRU 제거)
        if len(self.tlb) >= self.tlb_capacity:
            evicted_page, evicted_frame = self.tlb.popitem(last=False)
            print(f"  Evict LRU: page {evicted_page} -> frame {evicted_frame}")

        self.tlb[page_number] = frame
        print(f"TLB MISS - page {page_number} -> frame {frame} | TLB={list(self.tlb.keys())}")
        return frame

    def report(self):
        total = self.hits + self.misses
        hit_rate = (self.hits / total) if total else 0.0
        avg_cost = (self.total_cost / total) if total else 0.0

        print("\n[Report]")
        print(f"  TLB capacity   = {self.tlb_capacity}")
        print(f"  accesses       = {total}")
        print(f"  hits           = {self.hits}")
        print(f"  misses         = {self.misses}")
        print(f"  hit_rate       = {hit_rate:.3f}")
        print(f"  avg_cost(model)= {avg_cost:.3f} (hit=1, miss=2)")


def main():
    print("=== Lab 4: TLB Hit/Miss Simulation ===")

    # 간단 페이지 테이블: page -> frame
    page_table = {i: PTE(frame=(i * 3) % 10, valid=True) for i in range(10)}

    # 참조열(reference string): locality가 있으면 히트율이 올라감
    reference_pages = [2, 3, 2, 1, 2, 4, 2, 3, 5, 2, 1, 2, 6, 2, 3, 2]

    # TLB 용량을 바꿔가며 비교 가능
    sim = TLBSimulator(tlb_capacity=3, page_table=page_table)

    for p in reference_pages:
        sim.access(p)

    sim.report()

    # 용량을 늘리면 보통 hit_rate가 올라가는 경향이 있음
    print("\n--- Compare with bigger TLB capacity ---")
    sim2 = TLBSimulator(tlb_capacity=5, page_table=page_table)
    for p in reference_pages:
        sim2.access(p)
    sim2.report()


if __name__ == "__main__":
    main()
