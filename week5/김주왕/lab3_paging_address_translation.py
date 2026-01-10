# lab3_paging_address_translation.py
"""
Lab 3) Paging Address Translation
- 논리 주소(logical address)를 (page_number, offset)으로 분해
- 페이지 테이블(page table)로 frame_number를 찾기
- 물리 주소(physical address) = frame_number * page_size + offset

이 실습을 통해:
- "왜 프로세스 주소가 연속일 필요가 없는지"
- "페이지 테이블이 왜 필요한지"
를 매우 직관적으로 이해할 수 있습니다.
"""

from dataclasses import dataclass
from typing import Dict, Tuple, Optional


@dataclass
class PTE:
    """
    Page Table Entry (페이지 테이블 엔트리)
    - frame: 매핑된 프레임 번호
    - valid: 메모리에 올라와 있는지 여부(유효 비트)
    - writable: 쓰기 가능 여부(보호 비트의 한 예)
    - referenced: 접근 여부(참조 비트)
    - dirty: 수정 여부(더티 비트)
    """
    frame: int
    valid: bool = True
    writable: bool = True
    referenced: bool = False
    dirty: bool = False


class PagingMMU:
    """
    매우 단순화된 MMU(주소 변환 장치) 시뮬레이터.
    """
    def __init__(self, page_size: int, page_table: Dict[int, PTE]):
        self.page_size = page_size
        self.page_table = page_table

    def translate(self, logical_address: int, write: bool = False) -> Optional[int]:
        """
        논리 주소를 물리 주소로 변환합니다.

        write=False: read 접근
        write=True : write 접근(쓰기 권한 검사 + dirty 비트 설정)
        """
        # 1) logical_address를 페이지 번호와 오프셋으로 분해
        page_number = logical_address // self.page_size
        offset = logical_address % self.page_size

        print("\n[Translate]")
        print(f"  logical_address = {logical_address}")
        print(f"  page_number     = {page_number}")
        print(f"  offset          = {offset}")

        # 2) 페이지 테이블에서 엔트리 검색
        pte = self.page_table.get(page_number)
        if pte is None:
            print("  -> PAGE FAULT: No page table entry (invalid page number)")
            return None

        # 3) valid 비트 검사: 메모리에 없으면 페이지 폴트
        if not pte.valid:
            print("  -> PAGE FAULT: valid bit is 0 (page not in memory)")
            return None

        # 4) 보호 비트(쓰기 권한) 검사
        if write and not pte.writable:
            print("  -> PROTECTION FAULT: write not allowed on this page")
            return None

        # 5) referenced / dirty 비트 갱신(접근했으니 referenced=1)
        pte.referenced = True
        if write:
            pte.dirty = True

        # 6) 물리 주소 계산
        physical_address = pte.frame * self.page_size + offset
        print(f"  frame_number    = {pte.frame}")
        print(f"  physical_address= {physical_address}")

        return physical_address

    def print_page_table(self):
        print("\n[Page Table]")
        for pn in sorted(self.page_table.keys()):
            p = self.page_table[pn]
            print(
                f"  page={pn:2d} -> frame={p.frame:2d} | "
                f"V={int(p.valid)} W={int(p.writable)} R={int(p.referenced)} D={int(p.dirty)}"
            )


def main():
    print("=== Lab 3: Paging Address Translation ===")

    page_size = 16  # 페이지 크기(바이트 단위라고 가정)

    # 예시 페이지 테이블:
    # page 0 -> frame 5
    # page 1 -> frame 2
    # page 2 -> frame 8 (read-only)
    # page 3 -> not in memory (valid=0)
    page_table = {
        0: PTE(frame=5, valid=True, writable=True),
        1: PTE(frame=2, valid=True, writable=True),
        2: PTE(frame=8, valid=True, writable=False),  # 읽기 전용 페이지
        3: PTE(frame=1, valid=False, writable=True),  # 메모리에 없음(페이지 폴트 유도)
    }

    mmu = PagingMMU(page_size=page_size, page_table=page_table)
    mmu.print_page_table()

    # 테스트용 논리 주소들
    tests = [
        (0, False),    # page 0
        (18, False),   # page 1 offset 2
        (35, True),    # page 2 offset 3 (write 시도 -> protection fault)
        (50, False),   # page 3 offset 2 (valid=0 -> page fault)
        (999, False),  # page number가 없는 entry -> page fault
    ]

    for logical_addr, is_write in tests:
        kind = "WRITE" if is_write else "READ"
        print(f"\n--- {kind} access ---")
        mmu.translate(logical_addr, write=is_write)

    # 비트들이 어떻게 변했는지 확인
    mmu.print_page_table()


if __name__ == "__main__":
    main()
