# Chapter 14 가상 메모리 실습 세트 (5개)

이 실습 세트는 14장(가상 메모리)을 구현하기 위한 Python script 입니다.

- Lab 1: 연속 메모리 할당(First/Best/Worst Fit) + 외부 단편화 + Compaction
- Lab 2: 스와핑(Swap In/Out) 시뮬레이션 (메모리 부족 상황에서 프로세스 교체)
- Lab 3: 페이징 주소 변환 (논리주소 = 페이지번호 + 오프셋 → 물리주소 계산)
- Lab 4: TLB 히트/미스 시뮬레이션 (페이지 테이블 접근 비용을 캐시로 줄이는 이유)
- Lab 5: 스래싱(Thrashing) & 프레임 할당 (프레임 수/작업집합에 따른 페이지 폴트율 변화)

```bash
python lab1_contiguous_allocation.py
python lab2_swapping_sim.py
python lab3_paging_address_translation.py
python lab4_tlb_sim.py
python lab5_thrashing_and_frame_allocation.py
